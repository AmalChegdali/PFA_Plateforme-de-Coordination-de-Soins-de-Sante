"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { PatientSidebar } from "@/components/patient-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { translations } from "@/lib/translations"
import { mockCertificates } from "@/lib/mock-data"
import { Award, Download } from "lucide-react"

export default function PatientCertifications() {
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

  const handleDownload = (certificate) => {
    // In a real app, this would generate and download a PDF
    alert(`Downloading certificate issued on ${new Date(certificate.issueDate).toLocaleDateString()}`)
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString(lang === "ar" ? "ar-SA" : lang === "fr" ? "fr-FR" : "en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    })
  }

  if (!user) return null

  const patientCertificates = mockCertificates.filter((c) => c.patientId === user.id)

  return (
    <div className={`flex min-h-screen ${lang === "ar" ? "rtl" : ""}`} dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} lang={lang} onLangChange={setLang} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.myCertifications}</h1>
            <p className="text-muted-foreground">{t.certificationsDesc}</p>
          </div>

          {/* Certificates List */}
          <Card>
            <CardHeader>
              <CardTitle>{t.receivedCertificates}</CardTitle>
              <CardDescription>{t.allCertificatesFromProviders}</CardDescription>
            </CardHeader>
            <CardContent>
              {patientCertificates.length === 0 ? (
                <div className="text-center py-12">
                  <Award className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">{t.noCertificates}</p>
                  <p className="text-sm text-muted-foreground mt-1">{t.certificatesWillAppear}</p>
                </div>
              ) : (
                <>
                  {/* Desktop Table */}
                  <div className="hidden md:block overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="border-b">
                          <th className="text-left py-3 px-4 font-medium">{t.provider}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.issueDate}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.expirationDate}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.status}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.actions}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {patientCertificates.map((certificate) => (
                          <tr key={certificate.id} className="border-b hover:bg-muted/30">
                            <td className="py-4 px-4 font-medium">
                              Dr. {certificate.doctorId === "3" ? "Fatima Alami" : "Unknown"}
                            </td>
                            <td className="py-4 px-4">{formatDate(certificate.issueDate)}</td>
                            <td className="py-4 px-4">{formatDate(certificate.expirationDate)}</td>
                            <td className="py-4 px-4">
                              <Badge
                                variant={certificate.status === "active" ? "active" : "secondary"}
                                className="text-xs"
                              >
                                {certificate.status.toUpperCase()}
                              </Badge>
                            </td>
                            <td className="py-4 px-4">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleDownload(certificate)}
                                className="gap-2"
                              >
                                <Download className="h-4 w-4" />
                                {t.download}
                              </Button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Mobile Cards */}
                  <div className="md:hidden space-y-4">
                    {patientCertificates.map((certificate) => (
                      <Card key={certificate.id}>
                        <CardContent className="pt-6 space-y-3">
                          <div className="flex items-start justify-between">
                            <div>
                              <p className="font-medium">
                                Dr. {certificate.doctorId === "3" ? "Fatima Alami" : "Unknown"}
                              </p>
                              <p className="text-sm text-muted-foreground">
                                {t.issued}: {formatDate(certificate.issueDate)}
                              </p>
                              <p className="text-sm text-muted-foreground">
                                {t.expires}: {formatDate(certificate.expirationDate)}
                              </p>
                            </div>
                            <Badge
                              variant={certificate.status === "active" ? "active" : "secondary"}
                              className="text-xs"
                            >
                              {certificate.status.toUpperCase()}
                            </Badge>
                          </div>
                          <div className="pt-2 border-t">
                            <p className="text-sm text-muted-foreground line-clamp-2">{certificate.content}</p>
                          </div>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleDownload(certificate)}
                            className="w-full gap-2"
                          >
                            <Download className="h-4 w-4" />
                            {t.downloadPDF}
                          </Button>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                </>
              )}
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  )
}
