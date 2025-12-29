"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { ProviderSidebar } from "@/components/provider-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { useToast } from "@/hooks/use-toast"
import { useLanguage } from "@/contexts/LanguageContext"
import { Search, Plus, Eye, Edit, Trash2, Loader2 } from "lucide-react"

const API_BASE_URL = "http://localhost:8080/api"

export default function MedicalRecordsPage() {
  const { lang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [records, setRecords] = useState([])
  const [assignedPatients, setAssignedPatients] = useState([])
  const [modalMode, setModalMode] = useState(null)
  const [selectedRecord, setSelectedRecord] = useState(null)
  const [selectedPatient, setSelectedPatient] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [deleteDialog, setDeleteDialog] = useState({ open: false, recordId: null })
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
    fetchData()
  }, [router])

  const fetchData = async () => {
    setIsLoading(true)
    try {
      const token = localStorage.getItem("token")
      
      // Fetch assigned patients and all records in parallel
      const [patientsRes, recordsRes] = await Promise.all([
        fetch(`${API_BASE_URL}/providers/patients/assigned`, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        fetch(`${API_BASE_URL}/records`, {
          headers: { Authorization: `Bearer ${token}` }
        })
      ])

      if (patientsRes.ok) {
        const patientsData = await patientsRes.json()
        setAssignedPatients(patientsData)
      }

      if (recordsRes.ok) {
        const recordsData = await recordsRes.json()
        setRecords(recordsData)
      }
    } catch (error) {
      console.error("Fetch error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Failed to load data",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const generateRecordId = () => {
    return 'REC-' + Date.now() + '-' + Math.random().toString(36).substr(2, 6).toUpperCase()
  }

  const getPatientInfo = (patientId) => {
    return assignedPatients.find((p) => p.id === patientId)
  }

  const handleCreateRecord = (patient) => {
    setSelectedPatient(patient)
    setSelectedRecord(null)
    setModalMode("create")
  }

  const handleViewRecord = async (recordId) => {
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/records/${recordId}`, {
        headers: { Authorization: `Bearer ${token}` }
      })

      if (response.ok) {
        const recordData = await response.json()
        const patient = getPatientInfo(recordData.patientId)
        setSelectedRecord(recordData)
        setSelectedPatient(patient)
        setModalMode("view")
      } else {
        toast({
          title: t.error || "Error",
          description: "Failed to load record details",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Fetch record error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error",
        variant: "destructive",
      })
    }
  }

  const handleEditRecord = async (recordId) => {
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/records/${recordId}`, {
        headers: { Authorization: `Bearer ${token}` }
      })

      if (response.ok) {
        const recordData = await response.json()
        const patient = getPatientInfo(recordData.patientId)
        setSelectedRecord(recordData)
        setSelectedPatient(patient)
        setModalMode("edit")
      }
    } catch (error) {
      console.error("Fetch record error:", error)
    }
  }

  const handleDeleteRecord = async () => {
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/records/${deleteDialog.recordId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` }
      })

      if (response.ok) {
        toast({
          title: t.success || "Success",
          description: "Record deleted successfully",
        })
        await fetchData() // Refresh records
      } else {
        toast({
          title: t.error || "Error",
          description: "Failed to delete record",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Delete error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error",
        variant: "destructive",
      })
    } finally {
      setDeleteDialog({ open: false, recordId: null })
    }
  }

  const handleSaveRecord = async (e) => {
    e.preventDefault()
    setIsSaving(true)

    const formData = new FormData(e.target)
    const now = new Date().toISOString()
    
    const recordData = {
      recordId: modalMode === "create" ? generateRecordId() : selectedRecord.recordId,
      patientId: selectedPatient.id,
      providerId: user.id || user.providerId,
      recordType: formData.get("recordType"),
      visitDate: formData.get("visitDate") + "T" + (formData.get("visitTime") || "00:00") + ":00.000Z",
      diagnosis: formData.get("diagnosis"),
      content: {
        symptoms: formData.get("symptoms") || "",
        treatment: formData.get("treatment") || "",
        medications: formData.get("medications") || "",
        notes: formData.get("notes") || "",
      },
      createdAt: modalMode === "create" ? now : selectedRecord.createdAt,
      updatedAt: now,
    }

    try {
      const token = localStorage.getItem("token")
      
      if (modalMode === "create") {
        // Create new record
        const response = await fetch(`${API_BASE_URL}/providers/medical-records`, {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(recordData),
        })

        if (response.ok) {
          toast({
            title: t.success || "Success",
            description: "Medical record created successfully",
          })
          await fetchData()
        } else {
          const error = await response.json()
          toast({
            title: t.error || "Error",
            description: error.message || "Failed to create record",
            variant: "destructive",
          })
        }
      } else if (modalMode === "edit") {
        // Update existing record
        const response = await fetch(`${API_BASE_URL}/records/${selectedRecord.recordId}`, {
          method: "PUT",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(recordData),
        })

        if (response.ok) {
          toast({
            title: t.success || "Success",
            description: "Medical record updated successfully",
          })
          await fetchData()
        } else {
          toast({
            title: t.error || "Error",
            description: "Failed to update record",
            variant: "destructive",
          })
        }
      }

      setModalMode(null)
      setSelectedRecord(null)
      setSelectedPatient(null)
    } catch (error) {
      console.error("Save record error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error",
        variant: "destructive",
      })
    } finally {
      setIsSaving(false)
    }
  }

  const getRecordTypeColor = (type) => {
    const colors = {
      "consultation": "bg-blue-50 text-blue-700 border-blue-200",
      "lab": "bg-purple-50 text-purple-700 border-purple-200",
      "procedure": "bg-orange-50 text-orange-700 border-orange-200",
      "vaccination": "bg-green-50 text-green-700 border-green-200",
      "prescription": "bg-pink-50 text-pink-700 border-pink-200",
    }
    return colors[type?.toLowerCase()] || "bg-gray-50 text-gray-700 border-gray-200"
  }

  const formatDate = (dateString) => {
    if (!dateString) return "N/A"
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    })
  }

  if (!user || isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  const filteredPatients = assignedPatients.filter((patient) => {
    const query = searchQuery.toLowerCase()
    return (
      patient.firstName?.toLowerCase().includes(query) ||
      patient.lastName?.toLowerCase().includes(query) ||
      patient.fullName?.toLowerCase().includes(query) ||
      patient.email?.toLowerCase().includes(query)
    )
  })

  const filteredRecords = records.filter((record) => {
    if (!searchQuery.trim()) return true
    const query = searchQuery.toLowerCase()
    const patient = getPatientInfo(record.patientId)
    return (
      patient?.firstName?.toLowerCase().includes(query) ||
      patient?.lastName?.toLowerCase().includes(query) ||
      patient?.fullName?.toLowerCase().includes(query) ||
      patient?.email?.toLowerCase().includes(query) ||
      record.diagnosis?.toLowerCase().includes(query) ||
      record.recordType?.toLowerCase().includes(query)
    )
  })

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <ProviderSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-6xl mx-auto space-y-8">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.medicalRecords || "Medical Records"}</h1>
            <p className="text-muted-foreground">Manage patient medical records and health history</p>
          </div>

          {/* Search Bar */}
          <Card>
            <CardContent className="pt-6">
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder={t.searchPatient || "Search patients or records..."}
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
            </CardContent>
          </Card>

          {/* Assigned Patients Table */}
          <Card>
            <CardHeader>
              <CardTitle>Assigned Patients</CardTitle>
              <CardDescription>Select a patient to create a new medical record</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b">
                      <th className="text-left py-3 px-4 font-medium text-sm">Patient Name</th>
                      <th className="text-left py-3 px-4 font-medium text-sm">Email</th>
                      <th className="text-left py-3 px-4 font-medium text-sm">Phone</th>
                      <th className="text-right py-3 px-4 font-medium text-sm">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredPatients.length > 0 ? (
                      filteredPatients.map((patient) => (
                        <tr key={patient.id} className="border-b hover:bg-secondary/30 transition-colors">
                          <td className="py-3 px-4">
                            <p className="font-medium">
                              {patient.fullName !== "N/A" ? patient.fullName : `${patient.firstName || ""} ${patient.lastName || ""}`.trim()}
                            </p>
                          </td>
                          <td className="py-3 px-4 text-sm text-muted-foreground">{patient.email}</td>
                          <td className="py-3 px-4 text-sm text-muted-foreground">{patient.phone || "N/A"}</td>
                          <td className="py-3 px-4 text-right">
                            <Button onClick={() => handleCreateRecord(patient)} size="sm" className="gap-2">
                              <Plus className="h-4 w-4" />
                              Create Record
                            </Button>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="4" className="py-8 text-center text-muted-foreground">
                          {searchQuery ? "No patients found" : "No assigned patients"}
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>

          {/* Medical Records Table */}
          <Card>
            <CardHeader>
              <CardTitle>Medical Records</CardTitle>
              <CardDescription>View, edit, and delete medical records</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b">
                      <th className="text-left py-3 px-4 font-medium text-sm">Patient</th>
                      <th className="text-left py-3 px-4 font-medium text-sm">Type</th>
                      <th className="text-left py-3 px-4 font-medium text-sm">Diagnosis</th>
                      <th className="text-left py-3 px-4 font-medium text-sm">Visit Date</th>
                      <th className="text-right py-3 px-4 font-medium text-sm">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredRecords.length > 0 ? (
                      filteredRecords.map((record) => {
                        const patient = getPatientInfo(record.patientId)
                        return (
                          <tr key={record.recordId} className="border-b hover:bg-secondary/30 transition-colors">
                            <td className="py-3 px-4">
                              <p className="font-medium">
                                {patient?.fullName !== "N/A" ? patient?.fullName : patient?.email || "Unknown"}
                              </p>
                              <p className="text-xs text-muted-foreground">{patient?.email}</p>
                            </td>
                            <td className="py-3 px-4">
                              <Badge variant="outline" className={getRecordTypeColor(record.recordType)}>
                                {record.recordType}
                              </Badge>
                            </td>
                            <td className="py-3 px-4 text-sm">{record.diagnosis}</td>
                            <td className="py-3 px-4 text-sm text-muted-foreground">{formatDate(record.visitDate)}</td>
                            <td className="py-3 px-4 text-right">
                              <div className="flex items-center justify-end gap-2">
                                <Button
                                  variant="outline"
                                  size="icon"
                                  onClick={() => handleViewRecord(record.recordId)}
                                  title="View"
                                >
                                  <Eye className="h-4 w-4" />
                                </Button>
                                <Button
                                  variant="outline"
                                  size="icon"
                                  onClick={() => handleEditRecord(record.recordId)}
                                  title="Edit"
                                >
                                  <Edit className="h-4 w-4" />
                                </Button>
                                <Button
                                  variant="outline"
                                  size="icon"
                                  onClick={() => setDeleteDialog({ open: true, recordId: record.recordId })}
                                  title="Delete"
                                  className="text-destructive hover:text-destructive"
                                >
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </div>
                            </td>
                          </tr>
                        )
                      })
                    ) : (
                      <tr>
                        <td colSpan="5" className="py-8 text-center text-muted-foreground">
                          {searchQuery ? "No records found" : "No medical records yet"}
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>

      {/* Create/Edit/View Modal */}
      <Dialog open={modalMode !== null} onOpenChange={() => {
        setModalMode(null)
        setSelectedRecord(null)
        setSelectedPatient(null)
      }}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {modalMode === "create" && "Create Medical Record"}
              {modalMode === "edit" && "Edit Medical Record"}
              {modalMode === "view" && "View Medical Record"}
            </DialogTitle>
            <DialogDescription>
              {selectedPatient && (selectedPatient.fullName !== "N/A" ? selectedPatient.fullName : selectedPatient.email)}
            </DialogDescription>
          </DialogHeader>

          {modalMode === "view" ? (
            <div className="space-y-4">
              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground">Record Type</Label>
                  <p className="font-medium">{selectedRecord?.recordType}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Visit Date</Label>
                  <p className="font-medium">{formatDate(selectedRecord?.visitDate)}</p>
                </div>
              </div>

              <div>
                <Label className="text-muted-foreground">Diagnosis</Label>
                <p className="font-medium">{selectedRecord?.diagnosis}</p>
              </div>

              {selectedRecord?.content?.symptoms && (
                <div>
                  <Label className="text-muted-foreground">Symptoms</Label>
                  <p className="text-sm whitespace-pre-wrap">{selectedRecord.content.symptoms}</p>
                </div>
              )}

              {selectedRecord?.content?.treatment && (
                <div>
                  <Label className="text-muted-foreground">Treatment</Label>
                  <p className="text-sm whitespace-pre-wrap">{selectedRecord.content.treatment}</p>
                </div>
              )}

              {selectedRecord?.content?.medications && (
                <div>
                  <Label className="text-muted-foreground">Medications</Label>
                  <p className="text-sm whitespace-pre-wrap">{selectedRecord.content.medications}</p>
                </div>
              )}

              {selectedRecord?.content?.notes && (
                <div>
                  <Label className="text-muted-foreground">Notes</Label>
                  <p className="text-sm whitespace-pre-wrap">{selectedRecord.content.notes}</p>
                </div>
              )}

              <DialogFooter>
                <Button onClick={() => setModalMode(null)}>Close</Button>
              </DialogFooter>
            </div>
          ) : (
            <form onSubmit={handleSaveRecord}>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="recordType">Record Type *</Label>
                  <Select name="recordType" defaultValue={selectedRecord?.recordType || "Consultation"} required>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Consultation">Consultation</SelectItem>
                      <SelectItem value="Lab">Lab Test</SelectItem>
                      <SelectItem value="Procedure">Procedure</SelectItem>
                      <SelectItem value="Vaccination">Vaccination</SelectItem>
                      <SelectItem value="Prescription">Prescription</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="visitDate">Visit Date *</Label>
                    <Input
                      id="visitDate"
                      name="visitDate"
                      type="date"
                      defaultValue={selectedRecord?.visitDate?.split('T')[0] || ""}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="visitTime">Visit Time</Label>
                    <Input
                      id="visitTime"
                      name="visitTime"
                      type="time"
                      defaultValue={selectedRecord?.visitDate ? new Date(selectedRecord.visitDate).toTimeString().slice(0, 5) : ""}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="diagnosis">Diagnosis *</Label>
                  <Input
                    id="diagnosis"
                    name="diagnosis"
                    defaultValue={selectedRecord?.diagnosis || ""}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="symptoms">Symptoms</Label>
                  <Textarea
                    id="symptoms"
                    name="symptoms"
                    defaultValue={selectedRecord?.content?.symptoms || ""}
                    rows={3}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="treatment">Treatment</Label>
                  <Textarea
                    id="treatment"
                    name="treatment"
                    defaultValue={selectedRecord?.content?.treatment || ""}
                    rows={3}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="medications">Medications</Label>
                  <Textarea
                    id="medications"
                    name="medications"
                    defaultValue={selectedRecord?.content?.medications || ""}
                    rows={2}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="notes">Additional Notes</Label>
                  <Textarea
                    id="notes"
                    name="notes"
                    defaultValue={selectedRecord?.content?.notes || ""}
                    rows={3}
                  />
                </div>
              </div>

              <DialogFooter className="mt-6">
                <Button type="button" variant="outline" onClick={() => setModalMode(null)} disabled={isSaving}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isSaving}>
                  {isSaving ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Saving...
                    </>
                  ) : (
                    modalMode === "create" ? "Create Record" : "Update Record"
                  )}
                </Button>
              </DialogFooter>
            </form>
          )}
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialog.open} onOpenChange={(open) => setDeleteDialog({ open, recordId: null })}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Medical Record</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this medical record? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteRecord} className="bg-destructive hover:bg-destructive/90">
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}