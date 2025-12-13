"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { PatientSidebar } from "@/components/patient-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { translations } from "@/lib/translations"
import { mockNotifications } from "@/lib/mock-data"
import { Bell, Check } from "lucide-react"

export default function PatientNotifications() {
  const [lang, setLang] = useState("en")
  const [user, setUser] = useState(null)
  const [notifications, setNotifications] = useState([])
  const router = useRouter()
  const t = translations[lang]

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

    if (diffInHours < 24) {
      return `${diffInHours} hours ago`
    } else if (diffInHours < 48) {
      return "Yesterday"
    } else {
      return date.toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
      })
    }
  }

  if (!user) return null

  const unreadCount = notifications.filter((n) => !n.read).length

  return (
    <div className={`flex min-h-screen ${lang === "ar" ? "rtl" : ""}`} dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} lang={lang} onLangChange={setLang} />

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
            <p className="text-muted-foreground">Stay updated on your medical requests and appointments</p>
          </div>

          {/* Notifications List */}
          <Card>
            <CardHeader>
              <CardTitle>All Notifications</CardTitle>
              <CardDescription>Recent updates from your healthcare providers</CardDescription>
            </CardHeader>
            <CardContent>
              {notifications.length === 0 ? (
                <div className="text-center py-12">
                  <Bell className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">No notifications yet</p>
                  <p className="text-sm text-muted-foreground mt-1">
                    You'll receive notifications when doctors respond to your requests
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
                              Mark as read
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
