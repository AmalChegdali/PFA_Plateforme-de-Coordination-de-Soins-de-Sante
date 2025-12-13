"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { ProviderSidebar } from "@/components/provider-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useToast } from "@/hooks/use-toast"
import { translations } from "@/lib/translations"
import { mockUsers, mockRequests } from "@/lib/mock-data"
import { Users, UserMinus, UserPlus } from "lucide-react"

export default function ProviderPatients() {
  const [lang, setLang] = useState("en")
  const [user, setUser] = useState(null)
  const [assignedPatientIds, setAssignedPatientIds] = useState([])
  const router = useRouter()
  const { toast } = useToast()
  const t = translations[lang]

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

    // Initialize assigned patients
    const assigned = mockRequests
      .filter((r) => r.doctorId === parsedUser.id)
      .map((r) => r.patientId)
      .filter((id, index, self) => self.indexOf(id) === index)
    setAssignedPatientIds(assigned)
  }, [router])

  const handleAssign = (patientId) => {
    setAssignedPatientIds([...assignedPatientIds, patientId])
    toast({
      title: "Success",
      description: "Patient assigned successfully!",
    })
  }

  const handleUnassign = (patientId) => {
    setAssignedPatientIds(assignedPatientIds.filter((id) => id !== patientId))
    toast({
      title: "Success",
      description: "Patient unassigned successfully!",
    })
  }

  if (!user) return null

  const allPatients = mockUsers.filter((u) => u.role === "patient")
  const assignedPatients = allPatients.filter((p) => assignedPatientIds.includes(p.id))
  const unassignedPatients = allPatients.filter((p) => !assignedPatientIds.includes(p.id))

  const PatientRow = ({ patient, isAssigned }) => (
    <tr className="border-b hover:bg-muted/30">
      <td className="py-4 px-4">
        {patient.profile.firstName && patient.profile.lastName
          ? `${patient.profile.firstName} ${patient.profile.lastName}`
          : "Incomplete"}
      </td>
      <td className="py-4 px-4">{patient.email}</td>
      <td className="py-4 px-4">{patient.profile.phone || "-"}</td>
      <td className="py-4 px-4">
        <Badge variant={patient.profile.status === "active" ? "active" : "pending"} className="text-xs">
          {patient.profile.status?.toUpperCase()}
        </Badge>
      </td>
      <td className="py-4 px-4">
        {isAssigned ? (
          <Button variant="outline" size="sm" onClick={() => handleUnassign(patient.id)} className="gap-2">
            <UserMinus className="h-4 w-4" />
            Unassign
          </Button>
        ) : (
          <Button
            variant="default"
            size="sm"
            onClick={() => handleAssign(patient.id)}
            className="gap-2 bg-primary hover:bg-primary/90"
          >
            <UserPlus className="h-4 w-4" />
            Assign
          </Button>
        )}
      </td>
    </tr>
  )

  const PatientCard = ({ patient, isAssigned }) => (
    <Card>
      <CardContent className="pt-6 space-y-3">
        <div className="flex items-start justify-between">
          <div>
            <p className="font-medium">
              {patient.profile.firstName && patient.profile.lastName
                ? `${patient.profile.firstName} ${patient.profile.lastName}`
                : "Incomplete"}
            </p>
            <p className="text-sm text-muted-foreground">{patient.email}</p>
          </div>
          <Badge variant={patient.profile.status === "active" ? "active" : "pending"} className="text-xs">
            {patient.profile.status?.toUpperCase()}
          </Badge>
        </div>
        <p className="text-sm text-muted-foreground">{patient.profile.phone || "No phone"}</p>
        {isAssigned ? (
          <Button variant="outline" size="sm" onClick={() => handleUnassign(patient.id)} className="w-full gap-2">
            <UserMinus className="h-4 w-4" />
            Unassign
          </Button>
        ) : (
          <Button
            variant="default"
            size="sm"
            onClick={() => handleAssign(patient.id)}
            className="w-full gap-2 bg-primary hover:bg-primary/90"
          >
            <UserPlus className="h-4 w-4" />
            Assign
          </Button>
        )}
      </CardContent>
    </Card>
  )

  return (
    <div className={`flex min-h-screen ${lang === "ar" ? "rtl" : ""}`} dir={lang === "ar" ? "rtl" : "ltr"}>
      <ProviderSidebar user={user} lang={lang} onLangChange={setLang} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.myPatients}</h1>
            <p className="text-muted-foreground">Manage your assigned patients</p>
          </div>

          {/* Tabs */}
          <Tabs defaultValue="all" className="space-y-6">
            <TabsList>
              <TabsTrigger value="all">All ({allPatients.length})</TabsTrigger>
              <TabsTrigger value="assigned">My Assigned ({assignedPatients.length})</TabsTrigger>
              <TabsTrigger value="unassigned">Unassigned ({unassignedPatients.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="all">
              <Card>
                <CardHeader>
                  <CardTitle>All Patients</CardTitle>
                  <CardDescription>Complete list of registered patients</CardDescription>
                </CardHeader>
                <CardContent>
                  {/* Desktop Table */}
                  <div className="hidden md:block overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="border-b">
                          <th className="text-left py-3 px-4 font-medium">Name</th>
                          <th className="text-left py-3 px-4 font-medium">Email</th>
                          <th className="text-left py-3 px-4 font-medium">Phone</th>
                          <th className="text-left py-3 px-4 font-medium">Status</th>
                          <th className="text-left py-3 px-4 font-medium">Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {allPatients.map((patient) => (
                          <PatientRow
                            key={patient.id}
                            patient={patient}
                            isAssigned={assignedPatientIds.includes(patient.id)}
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
                        isAssigned={assignedPatientIds.includes(patient.id)}
                      />
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="assigned">
              <Card>
                <CardHeader>
                  <CardTitle>My Assigned Patients</CardTitle>
                  <CardDescription>Patients assigned to your care</CardDescription>
                </CardHeader>
                <CardContent>
                  {assignedPatients.length === 0 ? (
                    <div className="text-center py-12">
                      <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground">No assigned patients yet</p>
                    </div>
                  ) : (
                    <>
                      {/* Desktop Table */}
                      <div className="hidden md:block overflow-x-auto">
                        <table className="w-full">
                          <thead>
                            <tr className="border-b">
                              <th className="text-left py-3 px-4 font-medium">Name</th>
                              <th className="text-left py-3 px-4 font-medium">Email</th>
                              <th className="text-left py-3 px-4 font-medium">Phone</th>
                              <th className="text-left py-3 px-4 font-medium">Status</th>
                              <th className="text-left py-3 px-4 font-medium">Actions</th>
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
                  <CardTitle>Unassigned Patients</CardTitle>
                  <CardDescription>Patients available for assignment</CardDescription>
                </CardHeader>
                <CardContent>
                  {unassignedPatients.length === 0 ? (
                    <div className="text-center py-12">
                      <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground">All patients are assigned</p>
                    </div>
                  ) : (
                    <>
                      {/* Desktop Table */}
                      <div className="hidden md:block overflow-x-auto">
                        <table className="w-full">
                          <thead>
                            <tr className="border-b">
                              <th className="text-left py-3 px-4 font-medium">Name</th>
                              <th className="text-left py-3 px-4 font-medium">Email</th>
                              <th className="text-left py-3 px-4 font-medium">Phone</th>
                              <th className="text-left py-3 px-4 font-medium">Status</th>
                              <th className="text-left py-3 px-4 font-medium">Actions</th>
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
    </div>
  )
}
