"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { PatientSidebar } from "@/components/patient-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { useToast } from "@/hooks/use-toast"
import { useLanguage } from "@/contexts/LanguageContext"
import { mockRequests } from "@/lib/mock-data"
import { Plus, Filter, FileText } from "lucide-react"

export default function PatientRequests() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [filter, setFilter] = useState("all")
  const [isLoading, setIsLoading] = useState(false)
  const router = useRouter()
  const { toast } = useToast()

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

  const handleSubmitRequest = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    setTimeout(() => {
      toast({
        title: t.success,
        description: t.requestSubmitted,
      })
      setIsModalOpen(false)
      setIsLoading(false)
    }, 800)
  }

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

  const getPriorityColor = (priority) => {
    switch (priority) {
      case "urgent":
        return "text-red-600 bg-red-50 border-red-200"
      case "high":
        return "text-orange-600 bg-orange-50 border-orange-200"
      case "medium":
        return "text-yellow-600 bg-yellow-50 border-yellow-200"
      case "low":
        return "text-green-600 bg-green-50 border-green-200"
      default:
        return ""
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

  if (!user) return null

  const userRequests = mockRequests.filter((req) => req.patientId === user.id)
  const filteredRequests = filter === "all" ? userRequests : userRequests.filter((req) => req.status === filter)

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold text-balance">{t.myRequests}</h1>
              <p className="text-muted-foreground">{t.manageMedicalRequests}</p>
            </div>
            <Button
              onClick={() => setIsModalOpen(true)}
              className="gap-2 bg-primary hover:bg-primary/90 w-full md:w-auto"
            >
              <Plus className="h-4 w-4" />
              {t.newRequest}
            </Button>
          </div>

          {/* Filters */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Filter className="h-5 w-5" />
                <CardTitle className="text-lg">{t.filters}</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                <Button variant={filter === "all" ? "default" : "outline"} onClick={() => setFilter("all")} size="sm">
                  {t.all}
                </Button>
                <Button
                  variant={filter === "pending" ? "default" : "outline"}
                  onClick={() => setFilter("pending")}
                  size="sm"
                >
                  {t.pending}
                </Button>
                <Button
                  variant={filter === "in_progress" ? "default" : "outline"}
                  onClick={() => setFilter("in_progress")}
                  size="sm"
                >
                  {t.inProgress}
                </Button>
                <Button
                  variant={filter === "completed" ? "default" : "outline"}
                  onClick={() => setFilter("completed")}
                  size="sm"
                >
                  {t.completed}
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Requests Table */}
          <Card>
            <CardHeader>
              <CardTitle>{t.yourRequests} ({filteredRequests.length})</CardTitle>
              <CardDescription>{t.trackRequestsStatus}</CardDescription>
            </CardHeader>
            <CardContent>
              {filteredRequests.length === 0 ? (
                <div className="text-center py-12">
                  <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">{t.noRequestsFound}</p>
                  <p className="text-sm text-muted-foreground mt-1">{t.createNewRequest}</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {/* Desktop Table */}
                  <div className="hidden md:block overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="border-b">
                          <th className="text-left py-3 px-4 font-medium">{t.type}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.subject}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.status}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.priority}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.date}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {filteredRequests.map((request) => (
                          <tr key={request.id} className="border-b hover:bg-muted/30">
                            <td className="py-4 px-4 capitalize">{request.type}</td>
                            <td className="py-4 px-4 font-medium">{request.subject}</td>
                            <td className="py-4 px-4">
                              <Badge variant={getStatusVariant(request.status)} className="text-xs">
                                {request.status.replace("_", " ").toUpperCase()}
                              </Badge>
                            </td>
                            <td className="py-4 px-4">
                              <span
                                className={`text-xs font-semibold px-2 py-1 rounded border ${getPriorityColor(request.priority)}`}
                              >
                                {request.priority.toUpperCase()}
                              </span>
                            </td>
                            <td className="py-4 px-4 text-muted-foreground">{formatDate(request.createdAt)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Mobile Cards */}
                  <div className="md:hidden space-y-4">
                    {filteredRequests.map((request) => (
                      <Card key={request.id}>
                        <CardContent className="pt-6 space-y-3">
                          <div className="flex items-start justify-between">
                            <div>
                              <p className="font-medium">{request.subject}</p>
                              <p className="text-sm text-muted-foreground capitalize">{request.type}</p>
                            </div>
                            <Badge variant={getStatusVariant(request.status)} className="text-xs">
                              {request.status.replace("_", " ").toUpperCase()}
                            </Badge>
                          </div>
                          <div className="flex items-center justify-between text-sm">
                            <span
                              className={`text-xs font-semibold px-2 py-1 rounded border ${getPriorityColor(request.priority)}`}
                            >
                              {request.priority.toUpperCase()}
                            </span>
                            <span className="text-muted-foreground">{formatDate(request.createdAt)}</span>
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </main>

      {/* New Request Modal */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-2xl">{t.newRequest}</DialogTitle>
            <DialogDescription>{t.fillFormToSubmit}</DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmitRequest}>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="type">{t.requestType}</Label>
                <Select name="type" defaultValue="consultation" required>
                  <SelectTrigger id="type">
                    <SelectValue placeholder={t.selectType} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="consultation">{t.consultation}</SelectItem>
                    <SelectItem value="follow-up">{t.followUp}</SelectItem>
                    <SelectItem value="prescription">{t.prescription}</SelectItem>
                    <SelectItem value="certificate">{t.medicalCertificate}</SelectItem>
                    <SelectItem value="other">{t.other}</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="priority">{t.priority}</Label>
                <Select name="priority" defaultValue="medium" required>
                  <SelectTrigger id="priority">
                    <SelectValue placeholder={t.selectPriority} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="low">{t.low}</SelectItem>
                    <SelectItem value="medium">{t.medium}</SelectItem>
                    <SelectItem value="high">{t.high}</SelectItem>
                    <SelectItem value="urgent">{t.urgent}</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="subject">{t.subject}</Label>
                <Input id="subject" name="subject" placeholder={t.briefDescription} required />
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">{t.description}</Label>
                <Textarea
                  id="description"
                  name="description"
                  placeholder={t.detailedInformation}
                  rows={4}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="preferredDate">{t.preferredDate}</Label>
                <Input id="preferredDate" name="preferredDate" type="date" required />
              </div>

              <div className="space-y-2">
                <Label htmlFor="doctor">{t.doctorOptional}</Label>
                <Select name="doctor">
                  <SelectTrigger id="doctor">
                    <SelectValue placeholder={t.selectDoctor} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="3">Dr. Fatima Alami - Cardiology</SelectItem>
                    <SelectItem value="any">{t.anyAvailableDoctor}</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
                {t.cancel}
              </Button>
              <Button type="submit" className="bg-primary hover:bg-primary/90" disabled={isLoading}>
                {isLoading ? t.submitting : t.send}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
