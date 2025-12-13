"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { PatientSidebar } from "@/components/patient-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { translations } from "@/lib/translations"
import { FileText } from "lucide-react"

export default function MedicalHistory() {
  const [lang, setLang] = useState("en")
  const [user, setUser] = useState(null)
  const router = useRouter()
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

  if (!user) return null

  return (
    <div className={`flex min-h-screen ${lang === "ar" ? "rtl" : ""}`} dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} lang={lang} onLangChange={setLang} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.medicalHistory}</h1>
            <p className="text-muted-foreground">View your complete medical history</p>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Medical Records</CardTitle>
              <CardDescription>All your medical records in one place</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-center py-12">
                <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">No medical history available yet</p>
                <p className="text-sm text-muted-foreground mt-1">
                  Your medical records will appear here once you complete consultations
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  )
}
