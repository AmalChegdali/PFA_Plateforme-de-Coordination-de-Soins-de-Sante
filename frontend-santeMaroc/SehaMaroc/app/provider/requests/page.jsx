"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { ProviderSidebar } from "@/components/provider-sidebar"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
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
import { Textarea } from "@/components/ui/textarea"
import { Checkbox } from "@/components/ui/checkbox"
import { useToast } from "@/hooks/use-toast"
import { useLanguage } from "@/contexts/LanguageContext"
import { FileText, MessageSquare, Loader2, Send } from "lucide-react"

const API_BASE_URL = "http://localhost:8080/api/requests"

export default function ProviderRequests() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [requests, setRequests] = useState([])
  const [selectedRequest, setSelectedRequest] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isMessagesModalOpen, setIsMessagesModalOpen] = useState(false)
  const [generateCertificate, setGenerateCertificate] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isSending, setIsSending] = useState(false)
  const [newMessage, setNewMessage] = useState("")
  const router = useRouter()
  const { toast } = useToast()

  useEffect(() => {
    const userData = localStorage.getItem("user")
    const token = localStorage.getItem("token")
    
    if (!userData || !token) {
      router.push("/auth/provider")
      return
    }
    
    const parsedUser = JSON.parse(userData)
    
    if (parsedUser.role !== "PROVIDER" && parsedUser.role !== "provider") {
      router.push("/")
      return
    }
    
    setUser(parsedUser)
    fetchRequests()
  }, [router])

  const fetchRequests = async () => {
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
        setRequests(data)
      } else if (response.status === 401) {
        localStorage.removeItem("token")
        localStorage.removeItem("user")
        router.push("/auth/provider")
      } else {
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

  const fetchRequestDetails = async (requestId) => {
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/${requestId}`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (response.ok) {
        const data = await response.json()
        setSelectedRequest(data)
      } else {
        toast({
          title: t.error || "Error",
          description: "Failed to load request details",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Fetch request details error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error",
        variant: "destructive",
      })
    }
  }

  const handleViewMessages = async (request) => {
    await fetchRequestDetails(request.requestId)
    setIsMessagesModalOpen(true)
  }

  const handleRespond = (request) => {
    setSelectedRequest(request)
    setIsModalOpen(true)
    setGenerateCertificate(false)
  }

  const handleSendMessage = async (e) => {
    e.preventDefault()
    if (!newMessage.trim()) return

    setIsSending(true)
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/${selectedRequest.requestId}/messages`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ content: newMessage }),
      })

      if (response.ok) {
        toast({
          title: t.success || "Success",
          description: t.messageSent || "Message sent successfully",
        })
        setNewMessage("")
        // Refresh request details to show new message
        await fetchRequestDetails(selectedRequest.requestId)
      } else {
        toast({
          title: t.error || "Error",
          description: "Failed to send message",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Send message error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error",
        variant: "destructive",
      })
    } finally {
      setIsSending(false)
    }
  }

  const handleSubmitResponse = async (e) => {
    e.preventDefault()
    setIsSending(true)

    const formData = new FormData(e.target)
    const responseMessage = formData.get("response")

    try {
      const token = localStorage.getItem("token")
      
      // First, update the request status and add response message
      const respondResponse = await fetch(`${API_BASE_URL}/${selectedRequest.requestId}/respond`, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          status: "TRAITÉ",
          responseMessage: responseMessage,
        }),
      })

      if (!respondResponse.ok) {
        throw new Error("Failed to update request status")
      }

      // Then, send the message to the conversation thread
      await fetch(`${API_BASE_URL}/${selectedRequest.requestId}/messages`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ content: responseMessage }),
      })

      // Handle certificate if needed
      if (generateCertificate) {
        const certificateContent = formData.get("certificateContent")
        const expirationDate = formData.get("expirationDate")
        
        // TODO: Add certificate generation endpoint when available
        console.log("Certificate data:", { certificateContent, expirationDate })
      }

      toast({
        title: t.success || "Success",
        description: generateCertificate 
          ? (t.responseAndCertificate || "Response sent with certificate") 
          : (t.responseSent || "Response sent successfully"),
      })
      
      setIsModalOpen(false)
      setSelectedRequest(null)
      setGenerateCertificate(false)
      await fetchRequests() // Refresh the list
    } catch (error) {
      console.error("Submit response error:", error)
      toast({
        title: t.error || "Error",
        description: error.message || t.connectionError || "Failed to send response",
        variant: "destructive",
      })
    } finally {
      setIsSending(false)
    }
  }

  const getStatusVariant = (status) => {
    switch (status?.toLowerCase()) {
      case "en_attente":
      case "pending":
        return "pending"
      case "en_cours":
      case "in_progress":
        return "in-progress"
      case "termine":
      case "completed":
        return "completed"
      default:
        return "default"
    }
  }

  const getPriorityColor = (priority) => {
    switch (priority?.toLowerCase()) {
      case "urg":
      case "urgent":
        return "text-red-600 bg-red-50 border-red-200"
      case "high":
        return "text-orange-600 bg-orange-50 border-orange-200"
      case "med":
      case "medium":
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

  const allRequests = requests
  const pendingRequests = requests.filter((r) => r.status?.toLowerCase() === "en_attente" || r.status?.toLowerCase() === "pending")
  const inProgressRequests = requests.filter((r) => r.status?.toLowerCase() === "en_cours" || r.status?.toLowerCase() === "in_progress")
  const completedRequests = requests.filter((r) => r.status?.toLowerCase() === "termine" || r.status?.toLowerCase() === "completed")

  const RequestCard = ({ request }) => (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="pt-6 space-y-4">
        <div className="flex items-start justify-between">
          <div className="space-y-1 flex-1">
            <div className="flex items-center gap-2 flex-wrap">
              <Badge variant={getStatusVariant(request.status)} className="text-xs">
                {request.status?.toUpperCase() || "UNKNOWN"}
              </Badge>
              <span className={`text-xs font-semibold px-2 py-1 rounded border ${getPriorityColor(request.priority)}`}>
                {request.priority?.toUpperCase() || "N/A"}
              </span>
            </div>
            <h3 className="font-semibold text-lg">{request.subject}</h3>
            <p className="text-sm text-muted-foreground capitalize">{request.type || "N/A"}</p>
          </div>
        </div>

        <div className="space-y-2">
          <div className="flex items-center gap-2 text-sm">
            <span className="font-medium">{t.patientLabel || "Patient"}:</span>
            <span className="text-muted-foreground">{request.patientName || request.patientEmail}</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <span className="font-medium">{t.dateLabel || "Date"}:</span>
            <span className="text-muted-foreground">{formatDate(request.createdAt)}</span>
          </div>
          {request.preferredDate && (
            <div className="flex items-center gap-2 text-sm">
              <span className="font-medium">{t.preferredDateLabel || "Preferred Date"}:</span>
              <span className="text-muted-foreground">{formatDate(request.preferredDate)}</span>
            </div>
          )}
        </div>

        <div className="pt-2">
          <p className="text-sm text-muted-foreground line-clamp-2">{request.description}</p>
        </div>

        {request.responseMessage && (
          <div className="pt-2 border-t">
            <p className="text-sm font-medium mb-1">{t.responseLabel || "Response"}:</p>
            <p className="text-sm text-muted-foreground">{request.responseMessage}</p>
            {request.responseDate && (
              <p className="text-xs text-muted-foreground mt-1">
                {t.respondedOn || "Responded on"} {formatDate(request.responseDate)}
              </p>
            )}
          </div>
        )}

        <div className="flex gap-2">
          <Button 
            onClick={() => handleViewMessages(request)} 
            variant="outline"
            className="flex-1 gap-2"
          >
            <MessageSquare className="h-4 w-4" />
            {t.viewMessages || "Messages"} {request.messages?.length > 0 && `(${request.messages.length})`}
          </Button>
          <Button 
            onClick={() => handleRespond(request)} 
            className="flex-1 gap-2 bg-secondary hover:bg-secondary/90"
          >
            <Send className="h-4 w-4" />
            {t.respond || "Respond"}
          </Button>
        </div>
      </CardContent>
    </Card>
  )

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <ProviderSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.receivedRequests || "Received Requests"}</h1>
            <p className="text-muted-foreground">{t.respondToRequests || "Review and respond to patient requests"}</p>
          </div>

          {/* Tabs */}
          <Tabs defaultValue="all" className="space-y-6">
            <TabsList>
              <TabsTrigger value="all">{t.allRequests || "All"} ({allRequests.length})</TabsTrigger>
              <TabsTrigger value="pending">{t.pendingRequestsTab || "Pending"} ({pendingRequests.length})</TabsTrigger>
              <TabsTrigger value="in-progress">{t.inProgressTab || "In Progress"} ({inProgressRequests.length})</TabsTrigger>
              <TabsTrigger value="completed">{t.completedTab || "Completed"} ({completedRequests.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="all">
              {allRequests.length === 0 ? (
                <Card>
                  <CardContent className="py-12 text-center">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">{t.noRequestsAvailable || "No requests available"}</p>
                  </CardContent>
                </Card>
              ) : (
                <div className="grid md:grid-cols-2 gap-4">
                  {allRequests.map((request) => (
                    <RequestCard key={request.requestId} request={request} />
                  ))}
                </div>
              )}
            </TabsContent>

            <TabsContent value="pending">
              {pendingRequests.length === 0 ? (
                <Card>
                  <CardContent className="py-12 text-center">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">{t.noPendingRequests || "No pending requests"}</p>
                  </CardContent>
                </Card>
              ) : (
                <div className="grid md:grid-cols-2 gap-4">
                  {pendingRequests.map((request) => (
                    <RequestCard key={request.requestId} request={request} />
                  ))}
                </div>
              )}
            </TabsContent>

            <TabsContent value="in-progress">
              {inProgressRequests.length === 0 ? (
                <Card>
                  <CardContent className="py-12 text-center">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">{t.noInProgressRequests || "No in-progress requests"}</p>
                  </CardContent>
                </Card>
              ) : (
                <div className="grid md:grid-cols-2 gap-4">
                  {inProgressRequests.map((request) => (
                    <RequestCard key={request.requestId} request={request} />
                  ))}
                </div>
              )}
            </TabsContent>

            <TabsContent value="completed">
              {completedRequests.length === 0 ? (
                <Card>
                  <CardContent className="py-12 text-center">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">{t.noCompletedRequests || "No completed requests"}</p>
                  </CardContent>
                </Card>
              ) : (
                <div className="grid md:grid-cols-2 gap-4">
                  {completedRequests.map((request) => (
                    <RequestCard key={request.requestId} request={request} />
                  ))}
                </div>
              )}
            </TabsContent>
          </Tabs>
        </div>
      </main>

      {/* Messages Modal */}
      <Dialog open={isMessagesModalOpen} onOpenChange={setIsMessagesModalOpen}>
        <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{t.requestMessages || "Request Messages"}</DialogTitle>
            <DialogDescription>
              {selectedRequest?.subject}
            </DialogDescription>
          </DialogHeader>

          {selectedRequest && (
            <div className="space-y-4">
              {/* Request Info */}
              <Card className="bg-muted/50">
                <CardContent className="pt-6 space-y-2">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold">{t.patientLabel || "Patient"}:</span>
                    <span>{selectedRequest.patientName || selectedRequest.patientEmail}</span>
                  </div>
                  <div>
                    <span className="font-semibold">{t.description || "Description"}:</span>
                    <p className="text-sm text-muted-foreground mt-1">{selectedRequest.description}</p>
                  </div>
                </CardContent>
              </Card>

              {/* Messages Thread */}
              <div className="space-y-3 max-h-96 overflow-y-auto">
                {selectedRequest.messages?.length > 0 ? (
                  selectedRequest.messages.map((msg, idx) => (
                    <div key={idx} className={`p-3 rounded-lg ${msg.sender === "provider" ? "bg-primary/10 ml-8" : "bg-muted mr-8"}`}>
                      <p className="text-sm">{msg.content}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {msg.sender === "provider" ? "You" : selectedRequest.patientName} • {formatDate(msg.timestamp)}
                      </p>
                    </div>
                  ))
                ) : (
                  <p className="text-center text-muted-foreground py-8">No messages yet</p>
                )}
              </div>

              {/* Send Message Form */}
              <form onSubmit={handleSendMessage} className="flex gap-2">
                <Input
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder={t.typeMessage || "Type your message..."}
                  disabled={isSending}
                />
                <Button type="submit" disabled={isSending || !newMessage.trim()}>
                  {isSending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
                </Button>
              </form>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Response Modal */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-2xl">{t.respondToRequest || "Respond to Request"}</DialogTitle>
            <DialogDescription>{t.provideMedicalResponse || "Provide your medical response"}</DialogDescription>
          </DialogHeader>

          {selectedRequest && (
            <div className="space-y-4">
              {/* Request Summary */}
              <Card className="bg-muted/50">
                <CardContent className="pt-6 space-y-2">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold">{t.patientLabel || "Patient"}:</span>
                    <span>{selectedRequest.patientName || selectedRequest.patientEmail}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold">{t.subject || "Subject"}:</span>
                    <span>{selectedRequest.subject}</span>
                  </div>
                  <div>
                    <span className="font-semibold">{t.description || "Description"}:</span>
                    <p className="text-sm text-muted-foreground mt-1">{selectedRequest.description}</p>
                  </div>
                </CardContent>
              </Card>

              {/* Response Form */}
              <form onSubmit={handleSubmitResponse}>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="response">{t.response || "Response"}</Label>
                    <Textarea
                      id="response"
                      name="response"
                      placeholder={t.provideMedicalAdvice || "Provide your medical advice..."}
                      rows={6}
                      required
                    />
                  </div>

                  <div className="flex items-center space-x-2">
                    <Checkbox id="certificate" checked={generateCertificate} onCheckedChange={setGenerateCertificate} />
                    <label
                      htmlFor="certificate"
                      className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                    >
                      {t.generateMedicalCertificate || "Generate medical certificate"}
                    </label>
                  </div>

                  {generateCertificate && (
                    <div className="space-y-4 pl-6 border-l-2 border-primary">
                      <div className="space-y-2">
                        <Label htmlFor="certificateContent">{t.certificateContent || "Certificate Content"}</Label>
                        <Textarea
                          id="certificateContent"
                          name="certificateContent"
                          placeholder={t.certifyThat || "I certify that..."}
                          rows={4}
                          required={generateCertificate}
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="expirationDate">{t.expirationDate || "Expiration Date"}</Label>
                        <Input id="expirationDate" name="expirationDate" type="date" required={generateCertificate} />
                      </div>
                    </div>
                  )}
                </div>

                <DialogFooter className="mt-6">
                  <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)} disabled={isSending}>
                    {t.cancel || "Cancel"}
                  </Button>
                  <Button type="submit" className="bg-secondary hover:bg-secondary/90" disabled={isSending}>
                    {isSending ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        {t.sending || "Sending..."}
                      </>
                    ) : (
                      t.sendResponse || "Send Response"
                    )}
                  </Button>
                </DialogFooter>
              </form>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}