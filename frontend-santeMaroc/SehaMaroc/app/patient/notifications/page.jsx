"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { PatientSidebar } from "@/components/patient-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { useLanguage } from "@/contexts/LanguageContext"
import { useToast } from "@/hooks/use-toast"
import { Bell, Check, Loader2 } from "lucide-react"

const API_BASE_URL = "http://localhost:8081/api/notifications"

export default function PatientNotifications() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [notifications, setNotifications] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const router = useRouter()
  const { toast } = useToast()

  useEffect(() => {
    const userData = localStorage.getItem("user")
    const token = localStorage.getItem("token")
    
    if (!userData || !token) {
      router.push("/auth/patient")
      return
    }
    
    const parsedUser = JSON.parse(userData)
    
    if (parsedUser.role !== "ROLE_PATIENT" && parsedUser.role !== "patient") {
      router.push("/")
      return
    }
    
    setUser(parsedUser)
    fetchNotifications()
  }, [router])

  const fetchNotifications = async () => {
    setIsLoading(true)
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(API_BASE_URL, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (response.ok) {
        const data = await response.json()
        // Transform backend data to frontend format
        const transformedNotifications = data.map((notif) => ({
          id: notif.requestId,
          requestId: notif.requestId,
          message: notif.message || notif.responseMessage || "New response received",
          status: notif.status,
          responseMessage: notif.responseMessage,
          providerName: notif.providerName || notif.providerId || "Healthcare Provider",
          date: notif.updatedAt || notif.createdAt || new Date().toISOString(),
          read: false, // Backend doesn't track read status yet
          type: notif.type,
          subject: notif.subject,
          priority: notif.priority,
        }))
        setNotifications(transformedNotifications)
      } else if (response.status === 401) {
        localStorage.removeItem("token")
        localStorage.removeItem("user")
        router.push("/auth/patient")
      } else {
        toast({
          title: t.error || "Error",
          description: t.failedToLoadNotifications || "Failed to load notifications",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Fetch notifications error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error. Please try again.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const markAsRead = (id) => {
    // Mark as read locally (no backend endpoint yet)
    setNotifications(notifications.map((n) => (n.id === id ? { ...n, read: true } : n)))
  }

  const getStatusBadge = (status) => {
    switch (status?.toUpperCase()) {
      case "TRAITÉ":
      case "COMPLETED":
        return <Badge className="bg-green-100 text-green-800">TRAITÉ</Badge>
      case "EN_ATTENTE":
      case "PENDING":
        return <Badge className="bg-yellow-100 text-yellow-800">EN ATTENTE</Badge>
      case "EN_COURS":
      case "IN_PROGRESS":
        return <Badge className="bg-blue-100 text-blue-800">EN COURS</Badge>
      default:
        return <Badge variant="outline">{status || "UPDATE"}</Badge>
    }
  }

  const formatDate = (dateString) => {
    if (!dateString) return "N/A"
    
    const date = new Date(dateString)
    const now = new Date()
    const diffInHours = Math.floor((now - date) / (1000 * 60 * 60))

    const locale = lang === "fr" ? "fr-FR" : lang === "ar" ? "ar-MA" : "en-US"
    
    if (diffInHours < 1) {
      return t.justNow || "Just now"
    } else if (diffInHours < 24) {
      return `${diffInHours} ${t.hoursAgo || "hours ago"}`
    } else if (diffInHours < 48) {
      return t.yesterday || "Yesterday"
    } else {
      return date.toLocaleDateString(locale, {
        month: "short",
        day: "numeric",
        year: "numeric",
      })
    }
  }

  if (!user || isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  const unreadCount = notifications.filter((n) => !n.read).length

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-bold text-balance">{t.notifications || "Notifications"}</h1>
              {unreadCount > 0 && (
                <Badge variant="destructive" className="h-6 w-6 rounded-full p-0 flex items-center justify-center">
                  {unreadCount}
                </Badge>
              )}
            </div>
            <p className="text-muted-foreground">{t.stayUpdated || "Stay updated with your medical requests"}</p>
          </div>

          {/* Notifications List */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>{t.allNotifications || "All Notifications"}</CardTitle>
                  <CardDescription>{t.recentUpdates || "Recent updates from your healthcare providers"}</CardDescription>
                </div>
                <Button 
                  variant="outline" 
                  size="sm" 
                  onClick={fetchNotifications}
                  className="gap-2"
                >
                  <Bell className="h-4 w-4" />
                  {t.refresh || "Refresh"}
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {notifications.length === 0 ? (
                <div className="text-center py-12">
                  <Bell className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">{t.noNotificationsYet || "No notifications yet"}</p>
                  <p className="text-sm text-muted-foreground mt-1">
                    {t.receiveNotifications || "You'll receive notifications when providers respond to your requests"}
                  </p>
                </div>
              ) : (
                <div className="space-y-3">
                  {notifications.map((notification) => (
                    <Card
                      key={notification.id}
                      className={`${!notification.read ? "border-2 border-primary/30 bg-primary/5" : ""}`}
                    >
                      <CardContent className="pt-6">
                        <div className="flex items-start justify-between gap-4">
                          <div className="flex-1 space-y-3">
                            <div className="flex items-start gap-3">
                              {!notification.read && (
                                <div className="h-2 w-2 rounded-full bg-primary flex-shrink-0 mt-2" />
                              )}
                              <div className="flex-1 space-y-2">
                                <div className="flex items-center gap-2 flex-wrap">
                                  {getStatusBadge(notification.status)}
                                  {notification.subject && (
                                    <span className="text-sm font-medium">{notification.subject}</span>
                                  )}
                                </div>
                                <p className="font-medium text-balance">{notification.message}</p>
                                {notification.responseMessage && notification.responseMessage !== notification.message && (
                                  <div className="bg-muted/50 p-3 rounded-md">
                                    <p className="text-sm text-muted-foreground">
                                      <span className="font-semibold">{t.response || "Response"}:</span>{" "}
                                      {notification.responseMessage}
                                    </p>
                                  </div>
                                )}
                              </div>
                            </div>
                            <div className="flex items-center gap-4 text-sm text-muted-foreground ml-5">
                              <span className="flex items-center gap-1">
                                👨‍⚕️ {notification.providerName}
                              </span>
                              <span>•</span>
                              <span>{formatDate(notification.date)}</span>
                            </div>
                          </div>
                          {!notification.read && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => markAsRead(notification.id)}
                              className="gap-2 flex-shrink-0"
                            >
                              <Check className="h-4 w-4" />
                              {t.markAsReadButton || "Mark as read"}
                            </Button>
                          )}
                        </div>
                      </CardContent>
                    </Card>
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