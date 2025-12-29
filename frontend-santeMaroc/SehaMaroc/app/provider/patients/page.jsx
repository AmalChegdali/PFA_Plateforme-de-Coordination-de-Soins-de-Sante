"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { ProviderSidebar } from "@/components/provider-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
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
import { Users, UserMinus, UserPlus, Loader2, CheckCircle, XCircle } from "lucide-react"

const API_BASE_URL = "http://localhost:8080/api/providers/patients"

export default function ProviderPatients() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [allPatients, setAllPatients] = useState([])
  const [assignedPatients, setAssignedPatients] = useState([])
  const [unassignedPatients, setUnassignedPatients] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(null)
  const [suspendDialog, setSuspendDialog] = useState({ open: false, patient: null })
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
    fetchAllData()
  }, [router])

  const fetchAllData = async () => {
    setIsLoading(true)
    try {
      const token = localStorage.getItem("token")
      
      // Fetch all patients, assigned, and unassigned in parallel
      const [allRes, assignedRes, unassignedRes] = await Promise.all([
        fetch(`${API_BASE_URL}/all`, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        fetch(`${API_BASE_URL}/assigned`, {
          headers: { Authorization: `Bearer ${token}` }
        }),
        fetch(`${API_BASE_URL}/unassigned`, {
          headers: { Authorization: `Bearer ${token}` }
        })
      ])

      if (allRes.ok) setAllPatients(await allRes.json())
      if (assignedRes.ok) setAssignedPatients(await assignedRes.json())
      if (unassignedRes.ok) setUnassignedPatients(await unassignedRes.json())
    } catch (error) {
      console.error("Fetch error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Failed to load patients",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleAssign = async (patientId) => {
    setActionLoading(patientId)
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/${patientId}/assign`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (response.ok) {
        toast({
          title: t.success || "Success",
          description: t.patientAssignedSuccess || "Patient assigned successfully",
        })
        await fetchAllData()
      } else {
        const data = await response.json()
        toast({
          title: t.error || "Error",
          description: data.message || "Failed to assign patient",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Assign error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error",
        variant: "destructive",
      })
    } finally {
      setActionLoading(null)
    }
  }

  const handleUnassign = async (patientId) => {
    setActionLoading(patientId)
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/${patientId}/assign`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (response.ok) {
        toast({
          title: t.success || "Success",
          description: t.patientUnassignedSuccess || "Patient unassigned successfully",
        })
        await fetchAllData()
      } else {
        const data = await response.json()
        toast({
          title: t.error || "Error",
          description: data.message || "Failed to unassign patient",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Unassign error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error",
        variant: "destructive",
      })
    } finally {
      setActionLoading(null)
    }
  }

  const handleActivate = async (patientId) => {
    setActionLoading(patientId)
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/${patientId}/activate`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (response.ok) {
        toast({
          title: t.success || "Success",
          description: t.patientActivatedSuccess || "Patient activated successfully",
        })
        await fetchAllData()
      } else {
        const data = await response.json()
        toast({
          title: t.error || "Error",
          description: data.message || "Failed to activate patient",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Activate error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error",
        variant: "destructive",
      })
    } finally {
      setActionLoading(null)
    }
  }

  const handleSuspend = async (patientId) => {
    setActionLoading(patientId)
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/${patientId}/suspend`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json", // Crucial for @RequestBody
        },
        // Send the payload the backend expects
        body: JSON.stringify({ reason: "Suspended by provider" }), 
      })
  
      if (response.ok) {
        const data = await response.json() // Now this will be valid JSON
        toast({
          title: t.success || "Success",
          description: t.patientSuspendedSuccess || "Patient suspended successfully",
        })
        await fetchAllData()
      } else {
        // Handle the 400 error gracefully
        const errorData = await response.json().catch(() => ({ message: "Server error" }))
        toast({
          title: t.error || "Error",
          description: errorData.message || "Failed to suspend patient",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Suspend error:", error)
    } finally {
      setActionLoading(null)
      setSuspendDialog({ open: false, patient: null })
    }
  }

  const getStatusBadge = (status) => {
    switch (status) {
      case "ACTIVE":
        return <Badge className="bg-green-100 text-green-800 border-green-200">{status}</Badge>
      case "PENDING":
        return <Badge className="bg-yellow-100 text-yellow-800 border-yellow-200">{status}</Badge>
      case "SUSPENDED":
        return <Badge className="bg-red-100 text-red-800 border-red-200">{status}</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const PatientRow = ({ patient, isAssigned }) => {
    const isActionLoading = actionLoading === patient.id

    return (
      <tr className="border-b hover:bg-muted/30">
        <td className="py-4 px-4">
          {patient.fullName && patient.fullName !== "N/A"
            ? patient.fullName
            : patient.firstName && patient.lastName
            ? `${patient.firstName} ${patient.lastName}`
            : t.incomplete || "Incomplete"}
        </td>
        <td className="py-4 px-4">{patient.email}</td>
        <td className="py-4 px-4">{patient.phone || t.noPhone || "N/A"}</td>
        <td className="py-4 px-4">{getStatusBadge(patient.accountStatus)}</td>
        <td className="py-4 px-4">
          <div className="flex gap-2 flex-wrap">
            {/* Assign/Unassign Button */}
            {isAssigned ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleUnassign(patient.id)}
                disabled={isActionLoading}
                className="gap-2"
              >
                {isActionLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <UserMinus className="h-4 w-4" />
                )}
                {t.unassign || "Unassign"}
              </Button>
            ) : (
              <Button
                variant="default"
                size="sm"
                onClick={() => handleAssign(patient.id)}
                disabled={isActionLoading}
                className="gap-2 bg-primary hover:bg-primary/90"
              >
                {isActionLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <UserPlus className="h-4 w-4" />
                )}
                {t.assign || "Assign"}
              </Button>
            )}

            {/* Activate/Suspend Button */}
            {patient.accountStatus === "ACTIVE" ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setSuspendDialog({ open: true, patient })}
                disabled={isActionLoading}
                className="gap-2 text-red-600 hover:text-red-700 hover:bg-red-50"
              >
                {isActionLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <XCircle className="h-4 w-4" />
                )}
                {t.suspend || "Suspend"}
              </Button>
            ) : patient.accountStatus === "PENDING" || patient.accountStatus === "SUSPENDED" ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleActivate(patient.id)}
                disabled={isActionLoading}
                className="gap-2 text-green-600 hover:text-green-700 hover:bg-green-50"
              >
                {isActionLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <CheckCircle className="h-4 w-4" />
                )}
                {t.activate || "Activate"}
              </Button>
            ) : null}
          </div>
        </td>
      </tr>
    )
  }

  const PatientCard = ({ patient, isAssigned }) => {
    const isActionLoading = actionLoading === patient.id

    return (
      <Card>
        <CardContent className="pt-6 space-y-3">
          <div className="flex items-start justify-between">
            <div>
              <p className="font-medium">
                {patient.fullName && patient.fullName !== "N/A"
                  ? patient.fullName
                  : patient.firstName && patient.lastName
                  ? `${patient.firstName} ${patient.lastName}`
                  : t.incomplete || "Incomplete"}
              </p>
              <p className="text-sm text-muted-foreground">{patient.email}</p>
            </div>
            {getStatusBadge(patient.accountStatus)}
          </div>
          <p className="text-sm text-muted-foreground">{patient.phone || t.noPhone || "N/A"}</p>

          <div className="flex gap-2 flex-col">
            {/* Assign/Unassign Button */}
            {isAssigned ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleUnassign(patient.id)}
                disabled={isActionLoading}
                className="w-full gap-2"
              >
                {isActionLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <UserMinus className="h-4 w-4" />
                )}
                {t.unassign || "Unassign"}
              </Button>
            ) : (
              <Button
                variant="default"
                size="sm"
                onClick={() => handleAssign(patient.id)}
                disabled={isActionLoading}
                className="w-full gap-2 bg-primary hover:bg-primary/90"
              >
                {isActionLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <UserPlus className="h-4 w-4" />
                )}
                {t.assign || "Assign"}
              </Button>
            )}

            {/* Activate/Suspend Button */}
            {patient.accountStatus === "ACTIVE" ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setSuspendDialog({ open: true, patient })}
                disabled={isActionLoading}
                className="w-full gap-2 text-red-600 hover:text-red-700 hover:bg-red-50"
              >
                {isActionLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <XCircle className="h-4 w-4" />
                )}
                {t.suspend || "Suspend"}
              </Button>
            ) : patient.accountStatus === "PENDING" || patient.accountStatus === "SUSPENDED" ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleActivate(patient.id)}
                disabled={isActionLoading}
                className="w-full gap-2 text-green-600 hover:text-green-700 hover:bg-green-50"
              >
                {isActionLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <CheckCircle className="h-4 w-4" />
                )}
                {t.activate || "Activate"}
              </Button>
            ) : null}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (isLoading || !user) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <ProviderSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.myPatients || "My Patients"}</h1>
            <p className="text-muted-foreground">{t.manageAssignedPatients || "Manage your assigned patients"}</p>
          </div>

          {/* Tabs */}
          <Tabs defaultValue="all" className="space-y-6">
            <TabsList>
              <TabsTrigger value="all">
                {t.all || "All"} ({allPatients.length})
              </TabsTrigger>
              <TabsTrigger value="assigned">
                {t.myAssigned || "My Assigned"} ({assignedPatients.length})
              </TabsTrigger>
              <TabsTrigger value="unassigned">
                {t.unassigned || "Unassigned"} ({unassignedPatients.length})
              </TabsTrigger>
            </TabsList>

            <TabsContent value="all">
              <Card>
                <CardHeader>
                  <CardTitle>{t.allPatients || "All Patients"}</CardTitle>
                  <CardDescription>{t.completeListPatients || "Complete list of all patients"}</CardDescription>
                </CardHeader>
                <CardContent>
                  {allPatients.length === 0 ? (
                    <div className="text-center py-12">
                      <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground">{t.noPatientsFound || "No patients found"}</p>
                    </div>
                  ) : (
                    <>
                      {/* Desktop Table */}
                      <div className="hidden md:block overflow-x-auto">
                        <table className="w-full">
                          <thead>
                            <tr className="border-b">
                              <th className="text-left py-3 px-4 font-medium">{t.name || "Name"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.email || "Email"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.phone || "Phone"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.status || "Status"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.actions || "Actions"}</th>
                            </tr>
                          </thead>
                          <tbody>
                            {allPatients.map((patient) => (
                              <PatientRow
                                key={patient.id}
                                patient={patient}
                                isAssigned={assignedPatients.some((p) => p.id === patient.id)}
                              />
                            ))}
                          </tbody>
                        </table>
                      </div>

                      {/* Mobile Cards */}
                      <div className="md:hidden space-y-4">
                        {allPatients.map((patient) => (
                          <PatientCard
                            key={patient.id}
                            patient={patient}
                            isAssigned={assignedPatients.some((p) => p.id === patient.id)}
                          />
                        ))}
                      </div>
                    </>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="assigned">
              <Card>
                <CardHeader>
                  <CardTitle>{t.myAssignedPatients || "My Assigned Patients"}</CardTitle>
                  <CardDescription>
                    {t.patientsAssignedToCare || "Patients assigned to your care"}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {assignedPatients.length === 0 ? (
                    <div className="text-center py-12">
                      <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground">
                        {t.noAssignedPatientsYet || "No assigned patients yet"}
                      </p>
                    </div>
                  ) : (
                    <>
                      {/* Desktop Table */}
                      <div className="hidden md:block overflow-x-auto">
                        <table className="w-full">
                          <thead>
                            <tr className="border-b">
                              <th className="text-left py-3 px-4 font-medium">{t.name || "Name"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.email || "Email"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.phone || "Phone"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.status || "Status"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.actions || "Actions"}</th>
                            </tr>
                          </thead>
                          <tbody>
                            {assignedPatients.map((patient) => (
                              <PatientRow key={patient.id} patient={patient} isAssigned={true} />
                            ))}
                          </tbody>
                        </table>
                      </div>

                      {/* Mobile Cards */}
                      <div className="md:hidden space-y-4">
                        {assignedPatients.map((patient) => (
                          <PatientCard key={patient.id} patient={patient} isAssigned={true} />
                        ))}
                      </div>
                    </>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="unassigned">
              <Card>
                <CardHeader>
                  <CardTitle>{t.unassignedPatients || "Unassigned Patients"}</CardTitle>
                  <CardDescription>
                    {t.patientsAvailableForAssignment || "Patients available for assignment"}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {unassignedPatients.length === 0 ? (
                    <div className="text-center py-12">
                      <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground">
                        {t.allPatientsAssigned || "All patients are assigned"}
                      </p>
                    </div>
                  ) : (
                    <>
                      {/* Desktop Table */}
                      <div className="hidden md:block overflow-x-auto">
                        <table className="w-full">
                          <thead>
                            <tr className="border-b">
                              <th className="text-left py-3 px-4 font-medium">{t.name || "Name"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.email || "Email"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.phone || "Phone"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.status || "Status"}</th>
                              <th className="text-left py-3 px-4 font-medium">{t.actions || "Actions"}</th>
                            </tr>
                          </thead>
                          <tbody>
                            {unassignedPatients.map((patient) => (
                              <PatientRow key={patient.id} patient={patient} isAssigned={false} />
                            ))}
                          </tbody>
                        </table>
                      </div>

                      {/* Mobile Cards */}
                      <div className="md:hidden space-y-4">
                        {unassignedPatients.map((patient) => (
                          <PatientCard key={patient.id} patient={patient} isAssigned={false} />
                        ))}
                      </div>
                    </>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </main>

      {/* Suspend Confirmation Dialog */}
      <AlertDialog open={suspendDialog.open} onOpenChange={(open) => setSuspendDialog({ open, patient: null })}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{t.confirmSuspension || "Confirm Suspension"}</AlertDialogTitle>
            <AlertDialogDescription>
              {t.suspendPatientWarning ||
                `Are you sure you want to suspend ${
                  suspendDialog.patient?.fullName !== "N/A"
                    ? suspendDialog.patient?.fullName
                    : suspendDialog.patient?.email
                }? This action can be reversed later.`}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{t.cancel || "Cancel"}</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => suspendDialog.patient && handleSuspend(suspendDialog.patient.id)}
              className="bg-red-600 hover:bg-red-700"
            >
              {t.suspend || "Suspend"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}