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
import { translations } from "@/lib/translations"
import { mockRequests } from "@/lib/mock-data"
import { Plus, Filter, FileText } from "lucide-react"

export default function PatientRequests() {
  const [lang, setLang] = useState("en")
  const [user, setUser] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [filter, setFilter] = useState("all")
  const [isLoading, setIsLoading] = useState(false)
  const router = useRouter()
  const { toast } = useToast()
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
  }, [router])

  const handleSubmitRequest = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    setTimeout(() => {
      toast({
        title: "Success",
        description: "Your request has been submitted successfully!",
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
    return new Date(dateString).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    })
  }

  if (!user) return null

  const userRequests = mockRequests.filter((req) => req.patientId === user.id)
  const filteredRequests = filter === "all" ? userRequests : userRequests.filter((req) => req.status === filter)

  return (
    <div className={`flex min-h-screen ${lang === "ar" ? "rtl" : ""}`} dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} lang={lang} onLangChange={setLang} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold text-balance">{t.myRequests}</h1>
              <p className="text-muted-foreground">Manage your medical requests</p>
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
                <CardTitle className="text-lg">Filters</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                <Button variant={filter === "all" ? "default" : "outline"} onClick={() => setFilter("all")} size="sm">
                  All
                </Button>
                <Button
                  variant={filter === "pending" ? "default" : "outline"}
                  onClick={() => setFilter("pending")}
                  size="sm"
                >
                  Pending
                </Button>
                <Button
                  variant={filter === "in_progress" ? "default" : "outline"}
                  onClick={() => setFilter("in_progress")}
                  size="sm"
                >
                  In Progress
                </Button>
                <Button
                  variant={filter === "completed" ? "default" : "outline"}
                  onClick={() => setFilter("completed")}
                  size="sm"
                >
                  Completed
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Requests Table */}
          <Card>
            <CardHeader>
              <CardTitle>Your Requests ({filteredRequests.length})</CardTitle>
              <CardDescription>Track the status of all your medical requests</CardDescription>
            </CardHeader>
            <CardContent>
              {filteredRequests.length === 0 ? (
                <div className="text-center py-12">
                  <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">No requests found</p>
                  <p className="text-sm text-muted-foreground mt-1">Create a new request to get started</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {/* Desktop Table */}
                  <div className="hidden md:block overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="border-b">
                          <th className="text-left py-3 px-4 font-medium">Type</th>
                          <th className="text-left py-3 px-4 font-medium">Subject</th>
                          <th className="text-left py-3 px-4 font-medium">Status</th>
                          <th className="text-left py-3 px-4 font-medium">Priority</th>
                          <th className="text-left py-3 px-4 font-medium">Date</th>
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
            <DialogDescription>Fill out the form to submit a new medical request</DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmitRequest}>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="type">Request Type</Label>
                <Select name="type" defaultValue="consultation" required>
                  <SelectTrigger id="type">
                    <SelectValue placeholder="Select type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="consultation">Consultation</SelectItem>
                    <SelectItem value="follow-up">Follow-up</SelectItem>
                    <SelectItem value="prescription">Prescription</SelectItem>
                    <SelectItem value="certificate">Medical Certificate</SelectItem>
                    <SelectItem value="other">Other</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="priority">Priority</Label>
                <Select name="priority" defaultValue="medium" required>
                  <SelectTrigger id="priority">
                    <SelectValue placeholder="Select priority" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="low">Low</SelectItem>
                    <SelectItem value="medium">Medium</SelectItem>
                    <SelectItem value="high">High</SelectItem>
                    <SelectItem value="urgent">Urgent</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="subject">Subject</Label>
                <Input id="subject" name="subject" placeholder="Brief description of your request" required />
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  name="description"
                  placeholder="Provide detailed information about your request"
                  rows={4}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="preferredDate">Preferred Date</Label>
                <Input id="preferredDate" name="preferredDate" type="date" required />
              </div>

              <div className="space-y-2">
                <Label htmlFor="doctor">Doctor (Optional)</Label>
                <Select name="doctor">
                  <SelectTrigger id="doctor">
                    <SelectValue placeholder="Select a doctor" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="3">Dr. Fatima Alami - Cardiology</SelectItem>
                    <SelectItem value="any">Any Available Doctor</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
                {t.cancel}
              </Button>
              <Button type="submit" className="bg-primary hover:bg-primary/90" disabled={isLoading}>
                {isLoading ? "Submitting..." : t.send}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
