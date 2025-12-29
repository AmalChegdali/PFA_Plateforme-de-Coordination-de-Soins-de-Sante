"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { ProviderSidebar } from "@/components/provider-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { useLanguage } from "@/contexts/LanguageContext"
import { mockRequests, mockUsers } from "@/lib/mock-data"
import { Users, FileText, CheckCircle, Award, AlertCircle, Loader2 } from "lucide-react"

export default function ProviderDashboard() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const router = useRouter()

  useEffect(() => {
    // Changed from sessionStorage to localStorage
    const userData = localStorage.getItem("user")
    const token = localStorage.getItem("token")
    
    if (!userData || !token) {
      router.push("/auth/provider")
      return
    }
    
    const parsedUser = JSON.parse(userData)
    
    // Check for both "PROVIDER" (from backend) and "provider" (legacy)
    if (parsedUser.role !== "PROVIDER" && parsedUser.role !== "provider") {
      router.push("/")
      return
    }
    
    setUser(parsedUser)
    setIsLoading(false)
  }, [router])

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  if (!user) return null

  const assignedPatients = mockUsers
    .filter((u) => u.role === "patient")
    .filter((p) => {
      // In real app, check if patient is assigned to this provider
      return mockRequests.some((r) => r.patientId === p.id && r.doctorId === user.id)
    })

  const allPatients = mockUsers.filter((u) => u.role === "patient")
  const pendingRequests = mockRequests.filter((r) => r.status === "pending")
  const processedRequests = mockRequests.filter((r) => r.status === "completed")
  const urgentRequests = mockRequests.filter((r) => r.priority === "urgent" || r.priority === "high")

  const getPriorityColor = (priority) => {
    switch (priority) {
      case "urgent":
        return "text-red-600 bg-red-50 border-red-200"
      case "high":
        return "text-orange-600 bg-orange-50 border-orange-200"
      default:
        return ""
    }
  }

  const formatDate = (dateString) => {
    const locale = lang === "fr" ? "fr-FR" : lang === "ar" ? "ar-MA" : "en-US"
    return new Date(dateString).toLocaleDateString(locale, {
      month: "short",
      day: "numeric",
    })
  }

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <ProviderSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">
              {t.welcomeBack || "Welcome back"}, {t.dr || "Dr."} {user.profile?.lastName || user.email?.split('@')[0] || t.provider || "Provider"}
            </h1>
            <p className="text-muted-foreground">{t.managePatients || "Manage your patients and requests"}</p>
          </div>

          {/* Stats Cards */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {t.assignedPatients || "Assigned Patients"}
                </CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{assignedPatients.length}</div>
                <p className="text-xs text-muted-foreground mt-1">
                  {t.ofTotalPatients?.replace("{total}", allPatients.length) || `of ${allPatients.length} total`}
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {t.pendingRequests || "Pending Requests"}
                </CardTitle>
                <FileText className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{pendingRequests.length}</div>
                <p className="text-xs text-muted-foreground mt-1">{t.awaitingResponse || "Awaiting response"}</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {t.processedRequests || "Processed Requests"}
                </CardTitle>
                <CheckCircle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{processedRequests.length}</div>
                <p className="text-xs text-muted-foreground mt-1">{t.completedThisMonth || "Completed this month"}</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {t.issuedCertificates || "Issued Certificates"}
                </CardTitle>
                <Award className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">1</div>
                <p className="text-xs text-muted-foreground mt-1">{t.certificatesIssued || "Certificates issued"}</p>
              </CardContent>
            </Card>
          </div>

          {/* Urgent Requests */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <AlertCircle className="h-5 w-5 text-orange-600" />
                <CardTitle>{t.urgentRequests || "Urgent Requests"}</CardTitle>
              </div>
              <CardDescription>{t.highPriorityRequests || "High priority requests requiring attention"}</CardDescription>
            </CardHeader>
            <CardContent>
              {urgentRequests.length === 0 ? (
                <div className="text-center py-8">
                  <p className="text-muted-foreground">{t.noUrgentRequests || "No urgent requests at the moment"}</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {urgentRequests.map((request) => (
                    <div key={request.id} className="flex items-center justify-between p-4 border rounded-lg">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <p className="font-medium">{request.subject}</p>
                          <span
                            className={`text-xs font-semibold px-2 py-1 rounded border ${getPriorityColor(request.priority)}`}
                          >
                            {request.priority.toUpperCase()}
                          </span>
                        </div>
                        <p className="text-sm text-muted-foreground">{request.patientName}</p>
                      </div>
                      <div className="text-sm text-muted-foreground">{formatDate(request.createdAt)}</div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* New Patients */}
          <Card>
            <CardHeader>
              <CardTitle>{t.newPatients || "New Patients"}</CardTitle>
              <CardDescription>{t.recentlyRegistered || "Recently registered patients"}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {allPatients.slice(0, 3).map((patient) => (
                  <div key={patient.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="space-y-1">
                      <p className="font-medium">
                        {patient.profile.firstName && patient.profile.lastName
                          ? `${patient.profile.firstName} ${patient.profile.lastName}`
                          : patient.email}
                      </p>
                      <p className="text-sm text-muted-foreground">{patient.email}</p>
                    </div>
                    <Badge variant={patient.profile.status === "active" ? "active" : "pending"} className="text-xs">
                      {patient.profile.status?.toUpperCase()}
                    </Badge>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  )
}