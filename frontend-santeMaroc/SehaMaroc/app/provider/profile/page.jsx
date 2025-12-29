"use client"
import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { ProviderSidebar } from "@/components/provider-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { useToast } from "@/hooks/use-toast"
import { useLanguage } from "@/contexts/LanguageContext"
import { Loader2, AlertCircle, X } from "lucide-react"

const API_BASE_URL = "http://localhost:8082/api/auth"

export default function ProviderProfile() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [profile, setProfile] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [subSpecialties, setSubSpecialties] = useState([])
  const [stateLicenses, setStateLicenses] = useState([])
  const [newSubSpecialty, setNewSubSpecialty] = useState("")
  const [newLicense, setNewLicense] = useState("")
  const router = useRouter()
  const searchParams = useSearchParams()
  const { toast } = useToast()

  const shouldComplete = searchParams.get("complete") === "true"

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
    fetchProfile()
  }, [router])

  const fetchProfile = async () => {
    setIsLoading(true)
    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/profile`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })

      if (response.ok) {
        const data = await response.json()
        setProfile(data)
        setSubSpecialties(data.subSpecialties || [])
        setStateLicenses(data.stateLicenses || [])
      } else if (response.status === 401) {
        localStorage.removeItem("token")
        localStorage.removeItem("user")
        router.push("/auth/provider")
      } else {
        toast({
          title: t.error || "Error",
          description: t.failedToLoadProfile || "Failed to load profile",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Profile fetch error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error. Please try again.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsSaving(true)

    const formData = new FormData(e.target)
    const profileData = {
      fullName: formData.get("fullName"),
      professionalTitle: formData.get("professionalTitle"),
      specialty: formData.get("specialty"),
      subSpecialties: subSpecialties.length > 0 ? subSpecialties : null,
      stateLicenses: stateLicenses.length > 0 ? stateLicenses : null,
      primaryClinicName: formData.get("primaryClinicName") || null,
      clinicAddress: formData.get("clinicAddress") || null,
      contactNumber: formData.get("contactNumber"),
    }

    try {
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/complete-profile`, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(profileData),
      })

      const data = await response.json()

      if (response.ok) {
        setProfile(data)
        toast({
          title: t.success || "Success",
          description: t.profileUpdated || "Profile updated successfully",
        })
        
        if (shouldComplete) {
          router.push("/provider/dashboard")
        }
      } else {
        toast({
          title: t.error || "Error",
          description: data.message || t.failedToUpdateProfile || "Failed to update profile",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Profile update error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error. Please try again.",
        variant: "destructive",
      })
    } finally {
      setIsSaving(false)
    }
  }

  const addSubSpecialty = () => {
    if (newSubSpecialty && !subSpecialties.includes(newSubSpecialty)) {
      setSubSpecialties([...subSpecialties, newSubSpecialty])
      setNewSubSpecialty("")
    }
  }

  const removeSubSpecialty = (specialty) => {
    setSubSpecialties(subSpecialties.filter((s) => s !== specialty))
  }

  const addLicense = () => {
    if (newLicense && !stateLicenses.includes(newLicense)) {
      setStateLicenses([...stateLicenses, newLicense])
      setNewLicense("")
    }
  }

  const removeLicense = (license) => {
    setStateLicenses(stateLicenses.filter((l) => l !== license))
  }

  if (!user || isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  if (!profile) return null

  const isProfileComplete = profile.fullName && profile.professionalTitle && profile.specialty && profile.contactNumber

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <ProviderSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.myProfile || "My Profile"}</h1>
            <p className="text-muted-foreground">{t.manageProfessionalInfo || "Manage your professional information"}</p>
          </div>

          {/* Account Status */}
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">{t.accountStatus || "Account Status"}</p>
                  <p className="text-lg font-semibold">{profile.email}</p>
                </div>
                <Badge variant={profile.profileComplete ? "active" : "pending"}>
                  {profile.profileComplete ? "COMPLETE" : "INCOMPLETE"}
                </Badge>
              </div>
            </CardContent>
          </Card>

          {/* Incomplete Profile Alert */}
          {!isProfileComplete && (
            <Alert className="border-2 border-primary/50 bg-primary/5">
              <AlertCircle className="h-5 w-5 text-primary" />
              <AlertTitle className="text-lg font-semibold">
                {t.completeProfile || "Complete Your Profile"}
              </AlertTitle>
              <AlertDescription>
                {t.completeProfileMessage || "Please fill in all required fields to complete your profile"}
              </AlertDescription>
            </Alert>
          )}

          {/* Profile Form */}
          <form onSubmit={handleSubmit}>
            <Card>
              <CardHeader>
                <CardTitle>{t.professionalInformation || "Professional Information"}</CardTitle>
                <CardDescription>
                  {t.updateProfessionalDetails || "Update your professional details"}
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="fullName">{t.fullName || "Full Name"} *</Label>
                  <Input
                    id="fullName"
                    name="fullName"
                    defaultValue={profile.fullName || ""}
                    placeholder="Dr. Ahmed Allaoui"
                    required
                  />
                </div>

                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="professionalTitle">{t.professionalTitle || "Professional Title"} *</Label>
                    <Input
                      id="professionalTitle"
                      name="professionalTitle"
                      defaultValue={profile.professionalTitle || ""}
                      placeholder="Médecin généraliste"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="specialty">{t.specialty || "Specialty"} *</Label>
                    <Input
                      id="specialty"
                      name="specialty"
                      defaultValue={profile.specialty || ""}
                      placeholder="Cardiology"
                      required
                    />
                  </div>
                </div>

                {/* Sub-specialties */}
                <div className="space-y-3">
                  <Label>{t.subSpecialties || "Sub-specialties"}</Label>
                  <div className="flex gap-2">
                    <Input
                      placeholder={t.enterSubSpecialty || "Enter a sub-specialty"}
                      value={newSubSpecialty}
                      onChange={(e) => setNewSubSpecialty(e.target.value)}
                      onKeyPress={(e) => e.key === "Enter" && (e.preventDefault(), addSubSpecialty())}
                    />
                    <Button type="button" onClick={addSubSpecialty} variant="outline">
                      {t.add || "Add"}
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {subSpecialties.map((specialty, index) => (
                      <Badge key={index} variant="secondary" className="gap-1 px-3 py-1">
                        {specialty}
                        <button
                          type="button"
                          onClick={() => removeSubSpecialty(specialty)}
                          className="ml-1 hover:text-destructive"
                        >
                          <X className="h-3 w-3" />
                        </button>
                      </Badge>
                    ))}
                  </div>
                </div>

                {/* State Licenses */}
                <div className="space-y-3">
                  <Label>{t.stateLicenses || "State Licenses"}</Label>
                  <div className="flex gap-2">
                    <Input
                      placeholder={t.enterLicense || "Enter a license number"}
                      value={newLicense}
                      onChange={(e) => setNewLicense(e.target.value)}
                      onKeyPress={(e) => e.key === "Enter" && (e.preventDefault(), addLicense())}
                    />
                    <Button type="button" onClick={addLicense} variant="outline">
                      {t.add || "Add"}
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {stateLicenses.map((license, index) => (
                      <Badge key={index} variant="secondary" className="gap-1 px-3 py-1">
                        {license}
                        <button
                          type="button"
                          onClick={() => removeLicense(license)}
                          className="ml-1 hover:text-destructive"
                        >
                          <X className="h-3 w-3" />
                        </button>
                      </Badge>
                    ))}
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="contactNumber">{t.contactNumber || "Contact Number"} *</Label>
                  <Input
                    id="contactNumber"
                    name="contactNumber"
                    type="tel"
                    defaultValue={profile.contactNumber || ""}
                    placeholder="+212 6 11 22 33 44"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="primaryClinicName">{t.primaryClinicName || "Primary Clinic Name"}</Label>
                  <Input
                    id="primaryClinicName"
                    name="primaryClinicName"
                    defaultValue={profile.primaryClinicName || ""}
                    placeholder="Tetouan Clinic"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="clinicAddress">{t.clinicAddress || "Clinic Address"}</Label>
                  <Input
                    id="clinicAddress"
                    name="clinicAddress"
                    defaultValue={profile.clinicAddress || ""}
                    placeholder="123 Boulevard Mohammed V, Tetouan"
                  />
                </div>
              </CardContent>
            </Card>

            <div className="flex justify-end gap-4 mt-6">
              <Button
                type="submit"
                size="lg"
                className="bg-secondary hover:bg-secondary/90"
                disabled={isSaving}
              >
                {isSaving ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    {t.saving || "Saving..."}
                  </>
                ) : (
                  t.save || "Save Profile"
                )}
              </Button>
            </div>
          </form>
        </div>
      </main>
    </div>
  )
}