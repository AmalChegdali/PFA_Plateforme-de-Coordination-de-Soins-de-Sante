"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { PatientSidebar } from "@/components/patient-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { useLanguage } from "@/contexts/LanguageContext"
import { useToast } from "@/hooks/use-toast"
import { FileText, Loader2, Eye, Calendar, User } from "lucide-react"

const API_BASE_URL = "http://localhost:8081/api/patient"

export default function MedicalHistory() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [records, setRecords] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [selectedRecord, setSelectedRecord] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)
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
    fetchMedicalHistory()
  }, [router])

  const fetchMedicalHistory = async () => {
    setIsLoading(true)
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/medical-history`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (response.ok) {
        const data = await response.json()
        setRecords(data)
      } else if (response.status === 401) {
        localStorage.removeItem("token")
        localStorage.removeItem("user")
        router.push("/auth/patient")
      } else {
        toast({
          title: t.error || "Error",
          description: t.failedToLoadHistory || "Failed to load medical history",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Fetch medical history error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error. Please try again.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const getRecordTypeColor = (type) => {
    const colors = {
      "consultation": "bg-blue-100 text-blue-800 border-blue-200",
      "lab": "bg-purple-100 text-purple-800 border-purple-200",
      "procedure": "bg-orange-100 text-orange-800 border-orange-200",
      "vaccination": "bg-green-100 text-green-800 border-green-200",
      "prescription": "bg-pink-100 text-pink-800 border-pink-200",
    }
    return colors[type?.toLowerCase()] || "bg-gray-100 text-gray-800 border-gray-200"
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    })
  }

  const handleViewRecord = (record) => {
    setSelectedRecord(record)
    setModalOpen(true)
  }

  // Group records by year and month
  const groupedRecords = records.reduce((acc, record) => {
    const date = new Date(record.visitDate)
    const yearMonth = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
    const monthYear = date.toLocaleDateString("en-US", { year: "numeric", month: "long" })
    
    if (!acc[yearMonth]) {
      acc[yearMonth] = {
        label: monthYear,
        records: []
      }
    }
    acc[yearMonth].records.push(record)
    return acc
  }, {})

  // Sort by date (most recent first)
  const sortedGroups = Object.entries(groupedRecords).sort((a, b) => b[0].localeCompare(a[0]))

  if (isLoading || !user) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.medicalHistory || "Medical History"}</h1>
            <p className="text-muted-foreground">{t.viewCompleteHistory || "View your complete medical history"}</p>
          </div>

          {records.length === 0 ? (
            <Card>
              <CardHeader>
                <CardTitle>{t.medicalRecords || "Medical Records"}</CardTitle>
                <CardDescription>{t.allMedicalRecords || "All your medical records"}</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="text-center py-12">
                  <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">{t.noHistoryYet || "No medical history yet"}</p>
                  <p className="text-sm text-muted-foreground mt-1">
                    {t.recordsWillAppear || "Your medical records will appear here after your visits"}
                  </p>
                </div>
              </CardContent>
            </Card>
          ) : (
            <>
              {/* Summary Card */}
              <div className="grid gap-4 md:grid-cols-3">
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between pb-2">
                    <CardTitle className="text-sm font-medium text-muted-foreground">
                      {t.totalRecords || "Total Records"}
                    </CardTitle>
                    <FileText className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-3xl font-bold">{records.length}</div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between pb-2">
                    <CardTitle className="text-sm font-medium text-muted-foreground">
                      {t.lastVisit || "Last Visit"}
                    </CardTitle>
                    <Calendar className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-lg font-bold">
                      {new Date(records[0]?.visitDate).toLocaleDateString("en-US", { 
                        month: "short", 
                        day: "numeric",
                        year: "numeric"
                      })}
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between pb-2">
                    <CardTitle className="text-sm font-medium text-muted-foreground">
                      {t.providers || "Healthcare Providers"}
                    </CardTitle>
                    <User className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-3xl font-bold">
                      {new Set(records.map(r => r.providerId)).size}
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Records Timeline */}
              <div className="space-y-6">
                {sortedGroups.map(([key, group]) => (
                  <Card key={key}>
                    <CardHeader>
                      <CardTitle className="text-xl">{group.label}</CardTitle>
                      <CardDescription>
                        {group.records.length} {group.records.length === 1 ? "record" : "records"}
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        {group.records.map((record, index) => (
                          <div
                            key={record.recordId || `record-${index}`}
                            className="flex items-center justify-between p-4 border rounded-lg hover:bg-secondary/30 transition-colors cursor-pointer"
                            onClick={() => handleViewRecord(record)}
                          >
                            <div className="flex-1 min-w-0">
                              <div className="flex items-center gap-3 flex-wrap">
                                <Badge variant="outline" className={getRecordTypeColor(record.recordType)}>
                                  {record.recordType?.charAt(0).toUpperCase() + record.recordType?.slice(1)}
                                </Badge>
                                <div>
                                  <p className="font-medium text-balance">{record.diagnosis}</p>
                                  <p className="text-sm text-muted-foreground">
                                    {formatDate(record.visitDate)}
                                    {record.providerName && ` • Dr. ${record.providerName}`}
                                  </p>
                                </div>
                              </div>
                            </div>
                            <Button 
                              variant="outline" 
                              size="icon"
                              onClick={(e) => {
                                e.stopPropagation()
                                handleViewRecord(record)
                              }}
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                          </div>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </>
          )}
        </div>
      </main>

      {/* View Record Modal */}
      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{t.medicalRecordDetails || "Medical Record Details"}</DialogTitle>
            <DialogDescription>
              {selectedRecord?.diagnosis} • {selectedRecord?.visitDate && formatDate(selectedRecord.visitDate)}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">{t.recordType || "Record Type"}</Label>
                <p className="font-medium">{selectedRecord?.recordType}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">{t.visitDate || "Visit Date"}</Label>
                <p className="font-medium">
                  {selectedRecord?.visitDate && formatDate(selectedRecord.visitDate)}
                </p>
              </div>
            </div>

            <div>
              <Label className="text-muted-foreground">{t.diagnosis || "Diagnosis"}</Label>
              <p className="font-medium">{selectedRecord?.diagnosis}</p>
            </div>

            {selectedRecord?.providerName && (
              <div>
                <Label className="text-muted-foreground">{t.provider || "Healthcare Provider"}</Label>
                <p className="font-medium">Dr. {selectedRecord.providerName}</p>
              </div>
            )}

            {selectedRecord?.content?.symptoms && (
              <div>
                <Label className="text-muted-foreground">{t.symptoms || "Symptoms"}</Label>
                <p className="text-sm whitespace-pre-wrap">{selectedRecord.content.symptoms}</p>
              </div>
            )}

            {selectedRecord?.content?.treatment && (
              <div>
                <Label className="text-muted-foreground">{t.treatment || "Treatment"}</Label>
                <p className="text-sm whitespace-pre-wrap">{selectedRecord.content.treatment}</p>
              </div>
            )}

            {selectedRecord?.content?.medications && (
              <div>
                <Label className="text-muted-foreground">{t.medications || "Medications"}</Label>
                <p className="text-sm whitespace-pre-wrap">{selectedRecord.content.medications}</p>
              </div>
            )}

            {selectedRecord?.content?.notes && (
              <div>
                <Label className="text-muted-foreground">{t.notes || "Additional Notes"}</Label>
                <p className="text-sm whitespace-pre-wrap">{selectedRecord.content.notes}</p>
              </div>
            )}

            <div className="pt-4 border-t">
              <p className="text-xs text-muted-foreground">
                {t.recordCreated || "Record Created"}: {selectedRecord?.createdAt && formatDate(selectedRecord.createdAt)}
              </p>
            </div>
          </div>

          <div className="flex justify-end">
            <Button onClick={() => setModalOpen(false)}>
              {t.close || "Close"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}