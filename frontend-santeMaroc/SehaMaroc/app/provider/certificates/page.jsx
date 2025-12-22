"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { ProviderSidebar } from "@/components/provider-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { useToast } from "@/hooks/use-toast"
import { Award, Download, Plus, Trash2, Eye, Loader2, Printer } from "lucide-react"

const API_BASE_URL = "http://localhost:8080/api"

export default function ProviderCertificates() {
  const [user, setUser] = useState(null)
  const [searchQuery, setSearchQuery] = useState("")
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [modalMode, setModalMode] = useState("create")
  const [selectedPatient, setSelectedPatient] = useState(null)
  const [selectedCertificate, setSelectedCertificate] = useState(null)
  const [certificates, setCertificates] = useState([])
  const [patients, setPatients] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const router = useRouter()
  const { toast } = useToast()

  // Form state
  const [formData, setFormData] = useState({
    type: "MEDICAL",
    title: "",
    caseTreated: "",
    content: "",
    expiryDate: "",
    signature: "",
    certificateNumber: "",
    requestId: ""
  })

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
      await Promise.all([fetchPatients(), fetchCertificates()])
    } catch (error) {
      console.error("Failed to fetch data:", error)
    } finally {
      setIsLoading(false)
    }
  }

  const fetchPatients = async () => {
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/providers/patients/assigned`, {
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (response.ok) {
        const data = await response.json()
        setPatients(data)
      } else if (response.status === 401) {
        handleUnauthorized()
      } else {
        toast({
          title: "Error",
          description: "Failed to load patients",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error fetching patients:", error)
      toast({
        title: "Error",
        description: "Failed to load patients",
        variant: "destructive",
      })
    }
  }

  const fetchCertificates = async () => {
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/certificates`, {
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (response.ok) {
        const data = await response.json()
        setCertificates(data)
      } else if (response.status === 401) {
        handleUnauthorized()
      } else {
        toast({
          title: "Error",
          description: "Failed to load certificates",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error fetching certificates:", error)
      toast({
        title: "Error",
        description: "Failed to load certificates",
        variant: "destructive",
      })
    }
  }

  const handleUnauthorized = () => {
    localStorage.removeItem("token")
    localStorage.removeItem("user")
    router.push("/auth/provider")
  }

  // Filter patients based on search
  const filteredPatients = patients.filter((patient) => {
    const searchLower = searchQuery.toLowerCase()
    const fullName = `${patient.firstName || ""} ${patient.lastName || ""}`.toLowerCase()
    return fullName.includes(searchLower) || patient.email.toLowerCase().includes(searchLower)
  })

  // Filter certificates based on search
  const filteredCertificates = certificates.filter((cert) => {
    const searchLower = searchQuery.toLowerCase()
    return (
      cert.patientName?.toLowerCase().includes(searchLower) ||
      cert.title?.toLowerCase().includes(searchLower) ||
      cert.caseTreated?.toLowerCase().includes(searchLower) ||
      cert.certificateNumber?.toLowerCase().includes(searchLower)
    )
  })

  const handleCreateCertificate = (patient) => {
    setSelectedPatient(patient)
    setFormData({
      type: "MEDICAL",
      title: "Medical Certificate",
      caseTreated: "",
      content: "",
      expiryDate: "",
      signature: user.profile?.firstName && user.profile?.lastName 
        ? `Dr. ${user.profile.firstName} ${user.profile.lastName}`
        : "",
      certificateNumber: `CERT-${Date.now()}`,
      requestId: ""
    })
    setModalMode("create")
    setIsModalOpen(true)
  }

  const handleViewCertificate = async (certificate) => {
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/certificates/${certificate.id}`, {
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (response.ok) {
        const fullCertificate = await response.json()
        setSelectedCertificate(fullCertificate)
        setModalMode("view")
        setIsModalOpen(true)
      } else {
        toast({
          title: "Error",
          description: "Failed to load certificate details",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error fetching certificate:", error)
      toast({
        title: "Error",
        description: "Failed to load certificate details",
        variant: "destructive",
      })
    }
  }

  const handlePrintCertificate = async (certificate) => {
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/certificates/${certificate.id}/print`, {
        headers: {
          "Authorization": `Bearer ${token}`,
        },
      })

      if (response.ok) {
        const blob = await response.blob()
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `certificate-${certificate.certificateNumber}.pdf`
        document.body.appendChild(a)
        a.click()
        window.URL.revokeObjectURL(url)
        document.body.removeChild(a)
        
        toast({
          title: "Success",
          description: "Certificate downloaded successfully",
        })
      } else {
        toast({
          title: "Error",
          description: "Failed to print certificate",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error printing certificate:", error)
      toast({
        title: "Error",
        description: "Failed to print certificate",
        variant: "destructive",
      })
    }
  }

  const handleSaveCertificate = async () => {
    if (!formData.title || !formData.caseTreated || !formData.content) {
      toast({
        title: "Validation Error",
        description: "Please fill in all required fields",
        variant: "destructive",
      })
      return
    }

    setIsSaving(true)

    try {
      const token = localStorage.getItem("token")
      
      const certificateData = {
        patientId: selectedPatient.id,
        requestId: formData.requestId || null,
        type: formData.type,
        title: formData.title,
        caseTreated: formData.caseTreated,
        content: formData.content,
        expiryDate: formData.expiryDate || null,
        signature: formData.signature,
        certificateNumber: formData.certificateNumber
      }

      const response = await fetch(`${API_BASE_URL}/certificates`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(certificateData),
      })

      if (response.ok) {
        toast({
          title: "Success",
          description: "Certificate created successfully",
        })
        setIsModalOpen(false)
        fetchCertificates()
      } else {
        const errorData = await response.json().catch(() => null)
        toast({
          title: "Error",
          description: errorData?.message || "Failed to create certificate",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error creating certificate:", error)
      toast({
        title: "Error",
        description: "Failed to create certificate",
        variant: "destructive",
      })
    } finally {
      setIsSaving(false)
    }
  }

  const closeModal = () => {
    setIsModalOpen(false)
    setSelectedPatient(null)
    setSelectedCertificate(null)
    setFormData({
      type: "MEDICAL",
      title: "",
      caseTreated: "",
      content: "",
      expiryDate: "",
      signature: "",
      certificateNumber: "",
      requestId: ""
    })
  }

  const formatDate = (dateString) => {
    if (!dateString) return "-"
    return new Date(dateString).toLocaleDateString("en-US", {
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

  return (
    <div className="flex min-h-screen">
      <ProviderSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">Medical Certificates</h1>
            <p className="text-muted-foreground">Create and manage medical certificates for your patients</p>
          </div>

          {/* Search Bar */}
          <div>
            <Input
              placeholder="Search patients or certificates..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="max-w-md"
            />
          </div>

          {/* All Patients */}
          <Card>
            <CardHeader>
              <CardTitle>All Patients</CardTitle>
              <CardDescription>Select a patient to create a new certificate</CardDescription>
            </CardHeader>
            <CardContent>
              {filteredPatients.length === 0 ? (
                <div className="text-center py-12">
                  <Award className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">{searchQuery ? "No patients found" : "No patients available"}</p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="border-b">
                        <th className="text-left py-3 px-4 font-medium">Patient Name</th>
                        <th className="text-left py-3 px-4 font-medium">Email</th>
                        <th className="text-left py-3 px-4 font-medium">Phone</th>
                        <th className="text-left py-3 px-4 font-medium">Status</th>
                        <th className="text-left py-3 px-4 font-medium">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredPatients.map((patient) => (
                        <tr key={patient.id} className="border-b hover:bg-muted/30">
                          <td className="py-4 px-4 font-medium">{patient.fullName}</td>
                          <td className="py-4 px-4">{patient.email}</td>
                          <td className="py-4 px-4">{patient.phone || "-"}</td>
                          <td className="py-4 px-4">
                            <Badge
                              variant={patient.accountStatus === "ACTIVE" ? "active" : "pending"}
                              className="text-xs"
                            >
                              {patient.accountStatus}
                            </Badge>
                          </td>
                          <td className="py-4 px-4">
                            <Button
                              size="sm"
                              onClick={() => handleCreateCertificate(patient)}
                              className="gap-2 bg-primary hover:bg-primary/90"
                            >
                              <Plus className="h-4 w-4" />
                              Create
                            </Button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Issued Certificates */}
          <Card>
            <CardHeader>
              <CardTitle>Issued Certificates</CardTitle>
              <CardDescription>All certificates you have created</CardDescription>
            </CardHeader>
            <CardContent>
              {filteredCertificates.length === 0 ? (
                <div className="text-center py-12">
                  <Award className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">
                    {searchQuery ? "No certificates found" : "No certificates created yet"}
                  </p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="border-b">
                        <th className="text-left py-3 px-4 font-medium">Certificate #</th>
                        <th className="text-left py-3 px-4 font-medium">Patient</th>
                        <th className="text-left py-3 px-4 font-medium">Title</th>
                        <th className="text-left py-3 px-4 font-medium">Case Treated</th>
                        <th className="text-left py-3 px-4 font-medium">Issue Date</th>
                        <th className="text-left py-3 px-4 font-medium">Expiration</th>
                        <th className="text-left py-3 px-4 font-medium">Status</th>
                        <th className="text-left py-3 px-4 font-medium">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredCertificates.map((certificate) => (
                        <tr key={certificate.id} className="border-b hover:bg-muted/30">
                          <td className="py-4 px-4 font-mono text-sm">{certificate.certificateNumber}</td>
                          <td className="py-4 px-4 font-medium">{certificate.patientName}</td>
                          <td className="py-4 px-4">{certificate.title}</td>
                          <td className="py-4 px-4 text-sm line-clamp-1">{certificate.caseTreated}</td>
                          <td className="py-4 px-4">{formatDate(certificate.issueDate)}</td>
                          <td className="py-4 px-4">{formatDate(certificate.expiryDate)}</td>
                          <td className="py-4 px-4">
                            <Badge variant="active" className="text-xs">
                              {certificate.status || "ACTIVE"}
                            </Badge>
                          </td>
                          <td className="py-4 px-4">
                            <div className="flex gap-2">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleViewCertificate(certificate)}
                                title="View"
                              >
                                <Eye className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handlePrintCertificate(certificate)}
                                title="Print/Download"
                                className="gap-1"
                              >
                                <Printer className="h-4 w-4" />
                              </Button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Certificate Modal */}
        <Dialog open={isModalOpen} onOpenChange={closeModal}>
          <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>
                {modalMode === "create" && "Create Medical Certificate"}
                {modalMode === "view" && "View Certificate"}
              </DialogTitle>
              <DialogDescription>
                {selectedPatient && `Patient: ${selectedPatient.fullName}`}
                {selectedCertificate && `Certificate: ${selectedCertificate.certificateNumber}`}
              </DialogDescription>
            </DialogHeader>

            {modalMode === "view" ? (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label className="text-muted-foreground">Certificate Number</Label>
                    <p className="font-medium font-mono">{selectedCertificate?.certificateNumber}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Type</Label>
                    <p className="font-medium">{selectedCertificate?.type}</p>
                  </div>
                </div>
                <div>
                  <Label className="text-muted-foreground">Patient</Label>
                  <p className="font-medium">{selectedCertificate?.patientName}</p>
                  <p className="text-sm text-muted-foreground">{selectedCertificate?.patientEmail}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Provider</Label>
                  <p className="font-medium">{selectedCertificate?.providerName}</p>
                  <p className="text-sm text-muted-foreground">{selectedCertificate?.providerProfessionalTitle}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Title</Label>
                  <p className="font-medium">{selectedCertificate?.title}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Case Treated</Label>
                  <p className="font-medium">{selectedCertificate?.caseTreated}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Content</Label>
                  <p className="font-medium whitespace-pre-wrap">{selectedCertificate?.content}</p>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label className="text-muted-foreground">Issue Date</Label>
                    <p className="font-medium">{formatDate(selectedCertificate?.issueDate)}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Expiry Date</Label>
                    <p className="font-medium">{formatDate(selectedCertificate?.expiryDate)}</p>
                  </div>
                </div>
                <div>
                  <Label className="text-muted-foreground">Signature</Label>
                  <p className="font-medium italic">{selectedCertificate?.signature}</p>
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="type">Certificate Type *</Label>
                    <Select
                      value={formData.type}
                      onValueChange={(value) => setFormData({ ...formData, type: value })}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="MEDICAL">Medical Certificate</SelectItem>
                        <SelectItem value="FITNESS">Fitness Certificate</SelectItem>
                        <SelectItem value="SICK_LEAVE">Sick Leave Certificate</SelectItem>
                        <SelectItem value="VACCINATION">Vaccination Certificate</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="certificateNumber">Certificate Number *</Label>
                    <Input
                      id="certificateNumber"
                      value={formData.certificateNumber}
                      onChange={(e) => setFormData({ ...formData, certificateNumber: e.target.value })}
                      placeholder="CERT-XXXXX"
                      required
                    />
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="title">Title *</Label>
                  <Input
                    id="title"
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    placeholder="e.g., Medical Certificate"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="caseTreated">Case Treated *</Label>
                  <Input
                    id="caseTreated"
                    value={formData.caseTreated}
                    onChange={(e) => setFormData({ ...formData, caseTreated: e.target.value })}
                    placeholder="e.g., Common cold with 3 days rest"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="content">Certificate Content *</Label>
                  <Textarea
                    id="content"
                    value={formData.content}
                    onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                    placeholder="Enter the full certificate content..."
                    rows={6}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="expiryDate">Expiry Date</Label>
                  <Input
                    id="expiryDate"
                    type="date"
                    value={formData.expiryDate}
                    onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="signature">Signature *</Label>
                  <Input
                    id="signature"
                    value={formData.signature}
                    onChange={(e) => setFormData({ ...formData, signature: e.target.value })}
                    placeholder="Dr. John Doe"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="requestId">Request ID (Optional)</Label>
                  <Input
                    id="requestId"
                    value={formData.requestId}
                    onChange={(e) => setFormData({ ...formData, requestId: e.target.value })}
                    placeholder="Link to consultation request"
                  />
                </div>
              </div>
            )}

            <DialogFooter>
              {modalMode === "view" ? (
                <>
                  <Button variant="outline" onClick={closeModal}>Close</Button>
                  <Button onClick={() => handlePrintCertificate(selectedCertificate)} className="gap-2">
                    <Printer className="h-4 w-4" />
                    Print
                  </Button>
                </>
              ) : (
                <>
                  <Button variant="outline" onClick={closeModal}>Cancel</Button>
                  <Button onClick={handleSaveCertificate} disabled={isSaving}>
                    {isSaving ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Creating...
                      </>
                    ) : (
                      "Create Certificate"
                    )}
                  </Button>
                </>
              )}
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </main>
    </div>
  )
}