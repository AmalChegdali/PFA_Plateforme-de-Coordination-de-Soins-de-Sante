"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { ProviderSidebar } from "@/components/provider-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { useLanguage } from "@/contexts/LanguageContext"
import { mockCertificates } from "@/lib/mock-data"
import { Award, Download } from "lucide-react"

export default function ProviderCertificates() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const router = useRouter()

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

  const handleDownload = (certificate) => {
    // In a real app, this would generate and download a PDF
    alert(`${t.downloadingCertificate} ${certificate.patientName}`)
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

  const providerCertificates = mockCertificates.filter((c) => c.doctorId === user.id)

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <ProviderSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.certificates}</h1>
            <p className="text-muted-foreground">{t.viewDownloadCertificates}</p>
          </div>

          {/* Certificates List */}
          <Card>
            <CardHeader>
              <CardTitle>{t.issuedCertificatesTitle}</CardTitle>
              <CardDescription>{t.allIssuedCertificates}</CardDescription>
            </CardHeader>
            <CardContent>
              {providerCertificates.length === 0 ? (
                <div className="text-center py-12">
                  <Award className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">{t.noCertificatesYet}</p>
                  <p className="text-sm text-muted-foreground mt-1">
                    {t.certificatesWillAppear}
                  </p>
                </div>
              ) : (
                <>
                  {/* Desktop Table */}
                  <div className="hidden md:block overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="border-b">
                          <th className="text-left py-3 px-4 font-medium">{t.patient}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.issueDate}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.expirationDateLabel}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.status}</th>
                          <th className="text-left py-3 px-4 font-medium">{t.actions}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {providerCertificates.map((certificate) => (
                          <tr key={certificate.id} className="border-b hover:bg-muted/30">
                            <td className="py-4 px-4 font-medium">{certificate.patientName}</td>
                            <td className="py-4 px-4">{formatDate(certificate.issueDate)}</td>
                            <td className="py-4 px-4">{formatDate(certificate.expirationDate)}</td>
                            <td className="py-4 px-4">
                              <Badge variant="active" className="text-xs">
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
                                {t.downloadPDF}
                              </Button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Mobile Cards */}
                  <div className="md:hidden space-y-4">
                    {providerCertificates.map((certificate) => (
                      <Card key={certificate.id}>
                        <CardContent className="pt-6 space-y-3">
                          <div className="flex items-start justify-between">
                            <div>
                              <p className="font-medium">{certificate.patientName}</p>
                              <p className="text-sm text-muted-foreground">
                                {t.issued}: {formatDate(certificate.issueDate)}
                              </p>
                              <p className="text-sm text-muted-foreground">
                                {t.expires}: {formatDate(certificate.expirationDate)}
                              </p>
                            </div>
                            <Badge variant="active" className="text-xs">
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
                            Download PDF
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
