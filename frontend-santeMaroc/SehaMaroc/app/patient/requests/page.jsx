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
import { Plus, Filter, FileText, Loader2 } from "lucide-react"

const API_POST_URL = "http://localhost:8081/api/requests"
const API_GET_URL = "http://localhost:8080/api/requests"

export default function PatientRequests() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [requests, setRequests] = useState([])
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [filter, setFilter] = useState("all")
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
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
    /*
    if (parsedUser.role !== "ROLE_PATIENT" && parsedUser.role !== "patient") {
      router.push("/")
      return
    }
    */
    if (parsedUser.role !== "ROLE_PATIENT" && parsedUser.role !== "patient") {
      router.push("/")
      return
    }
    setUser(parsedUser)
    fetchRequests(parsedUser)
  }, [router])

  const fetchRequests = async (userData) => {
    setIsLoading(true)
    try {
      const token = localStorage.getItem("token")
      console.log("=== FETCH REQUESTS DEBUG ===")
      console.log("Token:", token ? `${token.substring(0, 20)}...` : "NO TOKEN")
      
      // First, fetch patient profile to get patient ID
      const profileResponse = await fetch("http://localhost:8081/api/patient/profile", {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      console.log("Profile response status:", profileResponse.status)

      if (!profileResponse.ok) {
        throw new Error("Failed to fetch profile")
      }

      const profileData = await profileResponse.json()
      console.log("Profile data:", profileData)
      
      const patientId = profileData.id || profileData.patientId
      console.log("Extracted patient ID:", patientId)
      
      if (!patientId) {
        console.error("Patient ID not found in profile:", profileData)
        toast({
          title: t.error || "Error",
          description: "Patient ID not found. Please try logging in again.",
          variant: "destructive",
        })
        setIsLoading(false)
        return
      }
      
      // Fetch requests using patient ID
      const requestUrl = `http://localhost:8080/api/requests/patient/${patientId}`
      console.log("Fetching requests from:", requestUrl)
      
      const response = await fetch(requestUrl, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      console.log("Requests response status:", response.status)

      if (response.ok) {
        const data = await response.json()
        console.log("Requests data:", data)
        setRequests(data)
      } else if (response.status === 401) {
        console.error("401 Unauthorized")
        localStorage.removeItem("token")
        localStorage.removeItem("user")
        router.push("/auth/patient")
      } else if (response.status === 403) {
        console.error("403 Forbidden - Token or permissions issue")
        const errorText = await response.text()
        console.error("Error response:", errorText)
        toast({
          title: t.error || "Error",
          description: "Access denied. Please check your permissions.",
          variant: "destructive",
        })
      } else {
        const errorText = await response.text()
        console.error("Error response:", errorText)
        toast({
          title: t.error || "Error",
          description: t.failedToLoadRequests || "Failed to load requests",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Fetch requests error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error. Please try again.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleSubmitRequest = async (e) => {
    e.preventDefault()
    setIsSubmitting(true)

    const formData = new FormData(e.target)
    const requestData = {
      type: formData.get("type"),
      priority: formData.get("priority"),
      subject: formData.get("subject"),
      description: formData.get("description"),
      preferredDate: formData.get("preferredDate"),
      targetProviderId: formData.get("doctor") !== "any" ? formData.get("doctor") : null,
    }

    try {
      const token = localStorage.getItem("token")
      const response = await fetch(API_POST_URL, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(requestData),
      })

      if (response.ok) {
        const data = await response.json()
        
        // Add the new request to the list
        setRequests([data, ...requests])
        
        toast({
          title: t.success || "Success",
          description: data.message || t.requestSubmitted || "Request submitted successfully",
        })
        
        setIsModalOpen(false)
        e.target.reset()
        
        // Refresh the requests list
        if (user) {
          fetchRequests(user)
        }
      } else {
        const error = await response.json()
        toast({
          title: t.error || "Error",
          description: error.message || t.requestFailed || "Failed to submit request",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Submit request error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error. Please try again.",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const getStatusVariant = (status) => {
    switch (status?.toLowerCase()) {
      case "queued":
      case "pending":
        return "pending"
      case "in_progress":
      case "processing":
        return "in-progress"
      case "completed":
      case "approved":
        return "completed"
      case "rejected":
        return "destructive"
      default:
        return "default"
    }
  }

  const getPriorityColor = (priority) => {
    switch (priority?.toLowerCase()) {
      case "urgent":
      case "urg":
        return "text-red-600 bg-red-50 border-red-200"
      case "high":
        return "text-orange-600 bg-orange-50 border-orange-200"
      case "medium":
      case "med":
        return "text-yellow-600 bg-yellow-50 border-yellow-200"
      case "low":
        return "text-green-600 bg-green-50 border-green-200"
      default:
        return "text-gray-600 bg-gray-50 border-gray-200"
    }
  }

  const formatDate = (dateString) => {
    if (!dateString) return "N/A"
    const locale = lang === "fr" ? "fr-FR" : lang === "ar" ? "ar-MA" : "en-US"
    return new Date(dateString).toLocaleDateString(locale, {
      month: "short",
      day: "numeric",
      year: "numeric",
    })
  }

  if (!user || isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  const filteredRequests = filter === "all" ? requests : requests.filter((req) => req.status?.toLowerCase() === filter)

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold text-balance">{t.myRequests || "My Requests"}</h1>
              <p className="text-muted-foreground">{t.manageMedicalRequests || "Manage your medical requests"}</p>
            </div>
            <Button
              onClick={() => setIsModalOpen(true)}
              className="gap-2 bg-primary hover:bg-primary/90 w-full md:w-auto"
            >
              <Plus className="h-4 w-4" />
              {t.newRequest || "New Request"}
            </Button>
          </div>

          {/* Filters */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Filter className="h-5 w-5" />
                <CardTitle className="text-lg">{t.filters || "Filters"}</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                <Button variant={filter === "all" ? "default" : "outline"} onClick={() => setFilter("all")} size="sm">
                  {t.all || "All"}
                </Button>
                <Button
                  variant={filter === "queued" ? "default" : "outline"}
                  onClick={() => setFilter("queued")}
                  size="sm"
                >
                  {t.queued || "Queued"}
                </Button>
                <Button
                  variant={filter === "pending" ? "default" : "outline"}
                  onClick={() => setFilter("pending")}
                  size="sm"
                >
                  {t.pending || "Pending"}
                </Button>
                <Button
                  variant={filter === "processing" ? "default" : "outline"}
                  onClick={() => setFilter("processing")}
                  size="sm"
                >
                  {t.processing || "Processing"}
                </Button>
                <Button
                  variant={filter === "completed" ? "default" : "outline"}
                  onClick={() => setFilter("completed")}
                  size="sm"
                >
                  {t.completed || "Completed"}
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Requests Table */}
          <Card>
            <CardHeader>
              <CardTitle>{t.yourRequests || "Your Requests"} ({filteredRequests.length})</CardTitle>
              <CardDescription>{t.trackRequestsStatus || "Track the status of your requests"}</CardDescription>
            </CardHeader>
            <CardContent>
              {filteredRequests.length === 0 ? (
                <div className="text-center py-12">
                  <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">
                    {requests.length === 0 
                      ? t.noRequestsYet || "No requests yet" 
                      : t.noRequestsFound || "No requests found for this filter"}
                  </p>
                  <p className="text-sm text-muted-foreground mt-1">
                    {t.createNewRequest || "Click 'New Request' to create your first request"}
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {/* Desktop Table */}
                  <div className="hidden md:block overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="border-b">
                          <th className="text-left py-3 px-4 font-medium">{t.type || "Type"}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.subject || "Subject"}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.status || "Status"}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.priority || "Priority"}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.date || "Date"}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {filteredRequests.map((request, index) => (
                          <tr key={request.requestId || `req-${index}`} className="border-b hover:bg-muted/30">
                            <td className="py-4 px-4 capitalize">{request.type || "N/A"}</td>
                            <td className="py-4 px-4 font-medium">{request.subject || "N/A"}</td>
                            <td className="py-4 px-4">
                              <Badge variant={getStatusVariant(request.status)} className="text-xs">
                                {request.status?.toUpperCase() || "UNKNOWN"}
                              </Badge>
                            </td>
                            <td className="py-4 px-4">
                              <span
                                className={`text-xs font-semibold px-2 py-1 rounded border ${getPriorityColor(request.priority)}`}
                              >
                                {request.priority?.toUpperCase() || "N/A"}
                              </span>
                            </td>
                            <td className="py-4 px-4 text-muted-foreground">
                              {formatDate(request.createdAt || request.preferredDate)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Mobile Cards */}
                  <div className="md:hidden space-y-4">
                    {filteredRequests.map((request, index) => (
                      <Card key={request.requestId || `req-${index}`}>
                        <CardContent className="pt-6 space-y-3">
                          <div className="flex items-start justify-between">
                            <div>
                              <p className="font-medium">{request.subject || "N/A"}</p>
                              <p className="text-sm text-muted-foreground capitalize">{request.type || "N/A"}</p>
                            </div>
                            <Badge variant={getStatusVariant(request.status)} className="text-xs">
                              {request.status?.toUpperCase() || "UNKNOWN"}
                            </Badge>
                          </div>
                          <div className="flex items-center justify-between text-sm">
                            <span
                              className={`text-xs font-semibold px-2 py-1 rounded border ${getPriorityColor(request.priority)}`}
                            >
                              {request.priority?.toUpperCase() || "N/A"}
                            </span>
                            <span className="text-muted-foreground">
                              {formatDate(request.createdAt || request.preferredDate)}
                            </span>
                          </div>
                          {request.responseMessage && (
                            <div className="pt-2 border-t">
                              <p className="text-sm text-muted-foreground">{request.responseMessage}</p>
                            </div>
                          )}
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
            <DialogTitle className="text-2xl">{t.newRequest || "New Request"}</DialogTitle>
            <DialogDescription>
              {t.fillFormToSubmit || "Fill in the form below to submit your medical request"}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmitRequest}>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="type">{t.requestType || "Request Type"} *</Label>
                <Select name="type" defaultValue="Consultation" required>
                  <SelectTrigger id="type">
                    <SelectValue placeholder={t.selectType || "Select type"} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Consultation">{t.consultation || "Consultation"}</SelectItem>
                    <SelectItem value="Follow-up">{t.followUp || "Follow-up"}</SelectItem>
                    <SelectItem value="Prescription">{t.prescription || "Prescription"}</SelectItem>
                    <SelectItem value="Certificate">{t.medicalCertificate || "Medical Certificate"}</SelectItem>
                    <SelectItem value="Other">{t.other || "Other"}</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="priority">{t.priority || "Priority"} *</Label>
                <Select name="priority" defaultValue="MED" required>
                  <SelectTrigger id="priority">
                    <SelectValue placeholder={t.selectPriority || "Select priority"} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="LOW">{t.low || "Low"}</SelectItem>
                    <SelectItem value="MED">{t.medium || "Medium"}</SelectItem>
                    <SelectItem value="HIGH">{t.high || "High"}</SelectItem>
                    <SelectItem value="URG">{t.urgent || "Urgent"}</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="subject">{t.subject || "Subject"} *</Label>
                <Input 
                  id="subject" 
                  name="subject" 
                  placeholder={t.briefDescription || "Brief description"} 
                  required 
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">{t.description || "Description"} *</Label>
                <Textarea
                  id="description"
                  name="description"
                  placeholder={t.detailedInformation || "Detailed information about your request"}
                  rows={4}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="preferredDate">{t.preferredDate || "Preferred Date"} *</Label>
                <Input 
                  id="preferredDate" 
                  name="preferredDate" 
                  type="date" 
                  min={new Date().toISOString().split('T')[0]}
                  required 
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="doctor">{t.doctorOptional || "Doctor (Optional)"}</Label>
                <Select name="doctor" defaultValue="any">
                  <SelectTrigger id="doctor">
                    <SelectValue placeholder={t.selectDoctor || "Select doctor"} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="any">{t.anyAvailableDoctor || "Any Available Doctor"}</SelectItem>
                    {/* TODO: Fetch and display available providers */}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <DialogFooter>
              <Button 
                type="button" 
                variant="outline" 
                onClick={() => setIsModalOpen(false)}
                disabled={isSubmitting}
              >
                {t.cancel || "Cancel"}
              </Button>
              <Button 
                type="submit" 
                className="bg-primary hover:bg-primary/90" 
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    {t.submitting || "Submitting..."}
                  </>
                ) : (
                  t.send || "Submit Request"
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}