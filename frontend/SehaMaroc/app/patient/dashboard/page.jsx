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
import { AlertCircle, FileText, Bell, Calendar, Plus } from "lucide-react"
import Link from "next/link"

export default function PatientDashboard() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const router = useRouter()

  useEffect(() => {
    const userData = sessionStorage.getItem("user")
    if (!userData) {
      router.push("/auth/patient")
      return
    }
    const parsedUser = JSON.parse(userData)
    if (parsedUser.role !== "patient") {
      router.push("/")
      return
    }
    setUser(parsedUser)
  }, [router])

  if (!user) return null

  const userRequests = mockRequests.filter((req) => req.patientId === user.id)
  const activeRequests = userRequests.filter((req) => req.status !== "completed").length
  const unreadNotifications = 1

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
              {t.welcomeBack}{user.profile?.firstName ? `, ${user.profile.firstName}` : ""}
            </h1>
            <p className="text-muted-foreground">{t.manageYourHealth}</p>
          </div>

          {/* Incomplete Profile Alert */}
          {!user.profile.profileComplete && (
            <Alert className="border-2 border-primary/50 bg-primary/5">
              <AlertCircle className="h-5 w-5 text-primary" />
              <AlertTitle className="text-lg font-semibold">{t.completeProfile}</AlertTitle>
              <AlertDescription className="mt-2">
                <p className="mb-3">{t.completeProfileMessage}</p>
                <Link href="/patient/profile?complete=true">
                  <Button className="bg-primary hover:bg-primary/90">{t.completeProfileNow}</Button>
                </Link>
              </AlertDescription>
            </Alert>
          )}

          {/* Stats Cards */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">{t.accountStatus}</CardTitle>
                <AlertCircle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <Badge variant={user.profile.status === "active" ? "active" : "pending"} className="text-sm">
                  {user.profile.status?.toUpperCase()}
                </Badge>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">{t.activeRequests}</CardTitle>
                <FileText className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{activeRequests}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">{t.unreadNotifications}</CardTitle>
                <Bell className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{unreadNotifications}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">{t.consultations}</CardTitle>
                <Calendar className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{userRequests.length}</div>
              </CardContent>
            </Card>
          </div>

          {/* Recent Requests */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>{t.recentRequests}</CardTitle>
                  <CardDescription>{t.last5Requests}</CardDescription>
                </div>
                <Link href="/patient/requests">
                  <Button className="gap-2 bg-primary hover:bg-primary/90">
                    <Plus className="h-4 w-4" />
                    {t.newRequest}
                  </Button>
                </Link>
              </div>
            </CardHeader>
            <CardContent>
              {userRequests.length === 0 ? (
                <div className="text-center py-12">
                  <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">{t.noRequests}</p>
                  <p className="text-sm text-muted-foreground mt-1">{t.createFirstRequest}</p>
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
