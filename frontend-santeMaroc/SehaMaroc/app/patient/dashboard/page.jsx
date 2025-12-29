"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { PatientSidebar } from "@/components/patient-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { useLanguage } from "@/contexts/LanguageContext"
import { mockRequests } from "@/lib/mock-data"
import { AlertCircle, FileText, Bell, Calendar, Plus, Lock } from "lucide-react"
import Link from "next/link"

export default function PatientDashboard() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [accountStatus, setAccountStatus] = useState(null)
  const router = useRouter()

  useEffect(() => {
    // Changed from sessionStorage to localStorage
    const userData = localStorage.getItem("user")
    const token = localStorage.getItem("token")
    
    if (!userData || !token) {
      router.push("/auth/patient")
      return
    }
    
    const parsedUser = JSON.parse(userData)
    
    // Check if user role is patient
    if (parsedUser.role !== "ROLE_PATIENT" && parsedUser.role !== "patient") {
      router.push("/")
      return
    }
    
    setUser(parsedUser)
    setAccountStatus(parsedUser.accountStatus)
  }, [router])

  if (!user) return null

  // Mock data - in real app, this would come from API
  const userRequests = mockRequests.filter((req) => req.patientId === user.id) || []
  const activeRequests = userRequests.filter((req) => req.status !== "completed").length
  const unreadNotifications = 1

  const isPending = accountStatus === "PENDING"

  const getStatusVariant = (status) => {
    switch (status) {
      case "pending":
        return "pending"
      case "in_progress":
        return "in-progress"
      case "completed":
        return "completed"
      default:
        return "default"
    }
  }

  const formatDate = (dateString) => {
    const locale = lang === "fr" ? "fr-FR" : lang === "ar" ? "ar-MA" : "en-US"
    return new Date(dateString).toLocaleDateString(locale, {
      month: "short",
      day: "numeric",
      year: "numeric",
    })
  }

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">
              {t.welcomeBack || "Welcome back"}{user.profile?.firstName ? `, ${user.profile.firstName}` : ""}
            </h1>
            <p className="text-muted-foreground">{t.manageYourHealth || "Manage your health records"}</p>
          </div>

          {/* Pending Account Alert */}
          {isPending && (
            <Alert className="border-2 border-yellow-500/50 bg-yellow-500/5">
              <Lock className="h-5 w-5 text-yellow-600" />
              <AlertTitle className="text-lg font-semibold text-yellow-800">
                {t.accountPending || "Account Pending Approval"}
              </AlertTitle>
              <AlertDescription className="mt-2 text-yellow-700">
                <p className="mb-3">
                  {t.accountPendingMessage || 
                    "Your account is pending provider approval. You can view and edit your profile, but access to medical history and booking appointments is currently restricted."}
                </p>
                <Link href="/patient/profile">
                  <Button variant="outline" className="border-yellow-600 text-yellow-700 hover:bg-yellow-50">
                    {t.viewProfile || "View Profile"}
                  </Button>
                </Link>
              </AlertDescription>
            </Alert>
          )}

          {/* Incomplete Profile Alert */}
          {!isPending && !user.profile?.profileComplete && (
            <Alert className="border-2 border-primary/50 bg-primary/5">
              <AlertCircle className="h-5 w-5 text-primary" />
              <AlertTitle className="text-lg font-semibold">{t.completeProfile || "Complete Your Profile"}</AlertTitle>
              <AlertDescription className="mt-2">
                <p className="mb-3">
                  {t.completeProfileMessage || "Please complete your profile to access all features"}
                </p>
                <Link href="/patient/profile?complete=true">
                  <Button className="bg-primary hover:bg-primary/90">
                    {t.completeProfileNow || "Complete Profile Now"}
                  </Button>
                </Link>
              </AlertDescription>
            </Alert>
          )}

          {/* Stats Cards */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {t.accountStatus || "Account Status"}
                </CardTitle>
                <AlertCircle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <Badge 
                  variant={accountStatus === "ACTIVE" ? "active" : "pending"} 
                  className="text-sm"
                >
                  {accountStatus || "PENDING"}
                </Badge>
              </CardContent>
            </Card>

            <Card className={isPending ? "opacity-50" : ""}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {t.activeRequests || "Active Requests"}
                </CardTitle>
                <FileText className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">
                  {isPending ? <Lock className="h-6 w-6 text-muted-foreground" /> : activeRequests}
                </div>
              </CardContent>
            </Card>

            <Card className={isPending ? "opacity-50" : ""}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {t.unreadNotifications || "Notifications"}
                </CardTitle>
                <Bell className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">
                  {isPending ? <Lock className="h-6 w-6 text-muted-foreground" /> : unreadNotifications}
                </div>
              </CardContent>
            </Card>

            <Card className={isPending ? "opacity-50" : ""}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {t.consultations || "Consultations"}
                </CardTitle>
                <Calendar className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">
                  {isPending ? <Lock className="h-6 w-6 text-muted-foreground" /> : userRequests.length}
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Recent Requests */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>{t.recentRequests || "Recent Requests"}</CardTitle>
                  <CardDescription>{t.last5Requests || "Your last 5 consultation requests"}</CardDescription>
                </div>
                {!isPending && (
                  <Link href="/patient/requests">
                    <Button className="gap-2 bg-primary hover:bg-primary/90">
                      <Plus className="h-4 w-4" />
                      {t.newRequest || "New Request"}
                    </Button>
                  </Link>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {isPending ? (
                <div className="text-center py-12">
                  <Lock className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="font-medium text-muted-foreground">
                    {t.featureLocked || "Feature Locked"}
                  </p>
                  <p className="text-sm text-muted-foreground mt-1">
                    {t.waitForApproval || "Please wait for provider approval to access this feature"}
                  </p>
                </div>
              ) : userRequests.length === 0 ? (
                <div className="text-center py-12">
                  <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">{t.noRequests || "No consultation requests yet"}</p>
                  <p className="text-sm text-muted-foreground mt-1">
                    {t.createFirstRequest || "Create your first request to get started"}
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {userRequests.slice(0, 5).map((request) => (
                    <div
                      key={request.id}
                      className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/30 transition-colors"
                    >
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <p className="font-medium">{request.subject}</p>
                          <Badge variant={getStatusVariant(request.status)} className="text-xs">
                            {request.status.replace("_", " ").toUpperCase()}
                          </Badge>
                        </div>
                        <p className="text-sm text-muted-foreground capitalize">{request.type}</p>
                      </div>
                      <div className="text-sm text-muted-foreground">{formatDate(request.createdAt)}</div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  )
}