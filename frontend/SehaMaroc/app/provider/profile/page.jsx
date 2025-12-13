"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { ProviderSidebar } from "@/components/provider-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useToast } from "@/hooks/use-toast"
import { useLanguage } from "@/contexts/LanguageContext"

export default function ProviderProfile() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
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

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    const formData = new FormData(e.target)
    const updatedProfile = {
      ...user.profile,
      firstName: formData.get("firstName"),
      lastName: formData.get("lastName"),
      specialization: formData.get("specialization"),
      phone: formData.get("phone"),
      address: formData.get("address"),
      city: formData.get("city"),
    }

    setTimeout(() => {
      const updatedUser = { ...user, profile: updatedProfile }
      sessionStorage.setItem("user", JSON.stringify(updatedUser))
      setUser(updatedUser)
      toast({
        title: t.success,
        description: t.profileUpdated,
      })
      setIsLoading(false)
    }, 800)
  }

  if (!user) return null

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <ProviderSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.myProfile}</h1>
            <p className="text-muted-foreground">{t.manageProfessionalInfo}</p>
          </div>

          {/* Profile Form */}
          <form onSubmit={handleSubmit}>
            <Card>
              <CardHeader>
                <CardTitle>{t.professionalInformation}</CardTitle>
                <CardDescription>{t.updateProfessionalDetails}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="firstName">{t.firstName}</Label>
                    <Input id="firstName" name="firstName" defaultValue={user.profile.firstName} required />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="lastName">{t.lastName}</Label>
                    <Input id="lastName" name="lastName" defaultValue={user.profile.lastName} required />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="specialization">{t.specialization}</Label>
                  <Input
                    id="specialization"
                    name="specialization"
                    placeholder="Cardiology"
                    defaultValue={user.profile.specialization}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="phone">{t.phone}</Label>
                  <Input
                    id="phone"
                    name="phone"
                    type="tel"
                    placeholder="+212 6 11 22 33 44"
                    defaultValue={user.profile.phone}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="address">{t.address}</Label>
                  <Input
                    id="address"
                    name="address"
                    placeholder="456 Boulevard Zerktouni"
                    defaultValue={user.profile.address}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="city">{t.city}</Label>
                  <Input id="city" name="city" placeholder="Casablanca" defaultValue={user.profile.city} required />
                </div>

                <div className="space-y-2">
                  <Label>{t.licenseNumber}</Label>
                  <Input value={user.profile.licenseNumber} disabled className="bg-muted" />
                  <p className="text-xs text-muted-foreground">{t.licenseNumberCannotChange}</p>
                </div>
              </CardContent>
            </Card>

            <div className="flex justify-end gap-4 mt-6">
              <Button type="submit" size="lg" className="bg-secondary hover:bg-secondary/90" disabled={isLoading}>
                {isLoading ? t.saving : t.save}
              </Button>
            </div>
          </form>
        </div>
      </main>
    </div>
  )
}
