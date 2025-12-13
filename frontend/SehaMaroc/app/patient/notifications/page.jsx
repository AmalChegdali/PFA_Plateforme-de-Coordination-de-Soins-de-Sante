"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { PatientSidebar } from "@/components/patient-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { useLanguage } from "@/contexts/LanguageContext"
import { mockNotifications } from "@/lib/mock-data"
import { Bell, Check } from "lucide-react"

export default function PatientNotifications() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [notifications, setNotifications] = useState([])
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
    setNotifications(mockNotifications.filter((n) => n.patientId === parsedUser.id))
  }, [router])

  const markAsRead = (id) => {
    setNotifications(notifications.map((n) => (n.id === id ? { ...n, read: true } : n)))
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffInHours = Math.floor((now - date) / (1000 * 60 * 60))

    const locale = lang === "fr" ? "fr-FR" : lang === "ar" ? "ar-MA" : "en-US"
    if (diffInHours < 24) {
      return `${diffInHours} ${t.hoursAgo}`
    } else if (diffInHours < 48) {
      return t.yesterday
    } else {
      return date.toLocaleDateString(locale, {
        month: "short",
        day: "numeric",
        year: "numeric",
      })
    }
  }

  if (!user) return null

  const unreadCount = notifications.filter((n) => !n.read).length

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-bold text-balance">{t.notifications}</h1>
              {unreadCount > 0 && (
                <Badge variant="destructive" className="h-6 w-6 rounded-full p-0 flex items-center justify-center">
                  {unreadCount}
                </Badge>
              )}
            </div>
            <p className="text-muted-foreground">{t.stayUpdated}</p>
          </div>

          {/* Notifications List */}
          <Card>
            <CardHeader>
              <CardTitle>{t.allNotifications}</CardTitle>
              <CardDescription>{t.recentUpdates}</CardDescription>
            </CardHeader>
            <CardContent>
              {notifications.length === 0 ? (
                <div className="text-center py-12">
                  <Bell className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">{t.noNotificationsYet}</p>
                  <p className="text-sm text-muted-foreground mt-1">
                    {t.receiveNotifications}
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
                          <div className="flex-1 space-y-2">
                            <div className="flex items-center gap-2">
                              {!notification.read && <div className="h-2 w-2 rounded-full bg-primary flex-shrink-0" />}
                              <p className="font-medium">{notification.message}</p>
                            </div>
                            <div className="flex items-center gap-4 text-sm text-muted-foreground">
                              <span>{notification.doctorName}</span>
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
                              {t.markAsReadButton}
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
