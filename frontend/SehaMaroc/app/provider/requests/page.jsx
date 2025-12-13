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
import { mockRequests } from "@/lib/mock-data"
import { FileText, MessageSquare } from "lucide-react"

export default function ProviderRequests() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [selectedRequest, setSelectedRequest] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [generateCertificate, setGenerateCertificate] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const router = useRouter()
  const { toast } = useToast()

  useEffect(() => {
    const userData = sessionStorage.getItem("user")
    if (!userData) {
      router.push("/auth/provider")
      return
    }
    const parsedUser = JSON.parse(userData)
    if (parsedUser.role !== "provider") {
      router.push("/")
      return
    }
    setUser(parsedUser)
  }, [router])

  const handleRespond = (request) => {
    setSelectedRequest(request)
    setIsModalOpen(true)
    setGenerateCertificate(false)
  }

  const handleSubmitResponse = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    setTimeout(() => {
      toast({
        title: t.success,
        description: generateCertificate ? t.responseAndCertificate : t.responseSent,
      })
      setIsModalOpen(false)
      setSelectedRequest(null)
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

  const allRequests = mockRequests
  const pendingRequests = allRequests.filter((r) => r.status === "pending")
  const inProgressRequests = allRequests.filter((r) => r.status === "in_progress")
  const completedRequests = allRequests.filter((r) => r.status === "completed")

  const RequestCard = ({ request }) => (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="pt-6 space-y-4">
        <div className="flex items-start justify-between">
          <div className="space-y-1 flex-1">
            <div className="flex items-center gap-2 flex-wrap">
              <Badge variant={getStatusVariant(request.status)} className="text-xs">
                {request.status.replace("_", " ").toUpperCase()}
              </Badge>
              <span className={`text-xs font-semibold px-2 py-1 rounded border ${getPriorityColor(request.priority)}`}>
                {request.priority.toUpperCase()}
              </span>
            </div>
            <h3 className="font-semibold text-lg">{request.subject}</h3>
            <p className="text-sm text-muted-foreground capitalize">{request.type}</p>
          </div>
        </div>

        <div className="space-y-2">
          <div className="flex items-center gap-2 text-sm">
            <span className="font-medium">{t.patientLabel}:</span>
            <span className="text-muted-foreground">{request.patientName}</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <span className="font-medium">{t.dateLabel}:</span>
            <span className="text-muted-foreground">{formatDate(request.createdAt)}</span>
          </div>
          {request.preferredDate && (
            <div className="flex items-center gap-2 text-sm">
              <span className="font-medium">{t.preferredDateLabel}:</span>
              <span className="text-muted-foreground">{formatDate(request.preferredDate)}</span>
            </div>
          )}
        </div>

        <div className="pt-2">
          <p className="text-sm text-muted-foreground line-clamp-2">{request.description}</p>
        </div>

        {request.status === "completed" && request.response && (
          <div className="pt-2 border-t">
            <p className="text-sm font-medium mb-1">{t.responseLabel}:</p>
            <p className="text-sm text-muted-foreground">{request.response}</p>
            <p className="text-xs text-muted-foreground mt-1">{t.respondedOn} {formatDate(request.respondedAt)}</p>
          </div>
        )}

        {request.status !== "completed" && (
          <Button onClick={() => handleRespond(request)} className="w-full gap-2 bg-secondary hover:bg-secondary/90">
            <MessageSquare className="h-4 w-4" />
            {t.respond}
          </Button>
        )}
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
            <h1 className="text-3xl font-bold text-balance">{t.receivedRequests}</h1>
            <p className="text-muted-foreground">{t.respondToRequests}</p>
          </div>

          {/* Tabs */}
          <Tabs defaultValue="all" className="space-y-6">
            <TabsList>
              <TabsTrigger value="all">{t.allRequests} ({allRequests.length})</TabsTrigger>
              <TabsTrigger value="pending">{t.pendingRequestsTab} ({pendingRequests.length})</TabsTrigger>
              <TabsTrigger value="in-progress">{t.inProgressTab} ({inProgressRequests.length})</TabsTrigger>
              <TabsTrigger value="completed">{t.completedTab} ({completedRequests.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="all">
              {allRequests.length === 0 ? (
                <Card>
                  <CardContent className="py-12 text-center">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">{t.noRequestsAvailable}</p>
                  </CardContent>
                </Card>
              ) : (
                <div className="grid md:grid-cols-2 gap-4">
                  {allRequests.map((request) => (
                    <RequestCard key={request.id} request={request} />
                  ))}
                </div>
              )}
            </TabsContent>

            <TabsContent value="pending">
              {pendingRequests.length === 0 ? (
                <Card>
                  <CardContent className="py-12 text-center">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">{t.noPendingRequests}</p>
                  </CardContent>
                </Card>
              ) : (
                <div className="grid md:grid-cols-2 gap-4">
                  {pendingRequests.map((request) => (
                    <RequestCard key={request.id} request={request} />
                  ))}
                </div>
              )}
            </TabsContent>

            <TabsContent value="in-progress">
              {inProgressRequests.length === 0 ? (
                <Card>
                  <CardContent className="py-12 text-center">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">{t.noInProgressRequests}</p>
                  </CardContent>
                </Card>
              ) : (
                <div className="grid md:grid-cols-2 gap-4">
                  {inProgressRequests.map((request) => (
                    <RequestCard key={request.id} request={request} />
                  ))}
                </div>
              )}
            </TabsContent>

            <TabsContent value="completed">
              {completedRequests.length === 0 ? (
                <Card>
                  <CardContent className="py-12 text-center">
                    <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">{t.noCompletedRequests}</p>
                  </CardContent>
                </Card>
              ) : (
                <div className="grid md:grid-cols-2 gap-4">
                  {completedRequests.map((request) => (
                    <RequestCard key={request.id} request={request} />
                  ))}
                </div>
              )}
            </TabsContent>
          </Tabs>
        </div>
      </main>

      {/* Response Modal */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-2xl">{t.respondToRequest}</DialogTitle>
            <DialogDescription>{t.provideMedicalResponse}</DialogDescription>
          </DialogHeader>

          {selectedRequest && (
            <div className="space-y-4">
              {/* Request Summary */}
              <Card className="bg-muted/50">
                <CardContent className="pt-6 space-y-2">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold">{t.patientLabel}:</span>
                    <span>{selectedRequest.patientName}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold">{t.subject}:</span>
                    <span>{selectedRequest.subject}</span>
                  </div>
                  <div>
                    <span className="font-semibold">{t.description}:</span>
                    <p className="text-sm text-muted-foreground mt-1">{selectedRequest.description}</p>
                  </div>
                </CardContent>
              </Card>

              {/* Response Form */}
              <form onSubmit={handleSubmitResponse}>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="response">{t.response}</Label>
                    <Textarea
                      id="response"
                      name="response"
                      placeholder={t.provideMedicalAdvice}
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
                      {t.generateMedicalCertificate}
                    </label>
                  </div>

                  {generateCertificate && (
                    <div className="space-y-4 pl-6 border-l-2 border-primary">
                      <div className="space-y-2">
                        <Label htmlFor="certificateContent">{t.certificateContent}</Label>
                        <Textarea
                          id="certificateContent"
                          name="certificateContent"
                          placeholder={t.certifyThat}
                          rows={4}
                          required={generateCertificate}
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="expirationDate">{t.expirationDate}</Label>
                        <Input id="expirationDate" name="expirationDate" type="date" required={generateCertificate} />
                      </div>
                    </div>
                  )}
                </div>

                <DialogFooter className="mt-6">
                  <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
                    {t.cancel}
                  </Button>
                  <Button type="submit" className="bg-secondary hover:bg-secondary/90" disabled={isLoading}>
                    {isLoading ? t.sending : t.sendResponse}
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
