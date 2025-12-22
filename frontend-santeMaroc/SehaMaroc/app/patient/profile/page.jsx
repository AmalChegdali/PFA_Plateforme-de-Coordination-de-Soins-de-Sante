"use client"
import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { PatientSidebar } from "@/components/patient-sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { useToast } from "@/hooks/use-toast"
import { useLanguage } from "@/contexts/LanguageContext"
import { AlertCircle, X, Loader2 } from "lucide-react"

const API_BASE_URL = "http://localhost:8081/api/patient"

export default function PatientProfile() {
  const { lang, setLang, t } = useLanguage()
  const [user, setUser] = useState(null)
  const [profile, setProfile] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [allergies, setAllergies] = useState([])
  const [chronicDiseases, setChronicDiseases] = useState([])
  const [newAllergy, setNewAllergy] = useState("")
  const [newDisease, setNewDisease] = useState("")
  const router = useRouter()
  const searchParams = useSearchParams()
  const { toast } = useToast()

  const shouldComplete = searchParams.get("complete") === "true"

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
        setAllergies(data.allergies || [])
        setChronicDiseases(data.chronicDiseases || [])
      } else if (response.status === 401) {
        localStorage.removeItem("token")
        localStorage.removeItem("user")
        router.push("/auth/patient")
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
      firstName: formData.get("firstName"),
      lastName: formData.get("lastName"),
      phone: formData.get("phone"),
      dateOfBirth: formData.get("dateOfBirth"),
      gender: formData.get("gender"),
      address: formData.get("address"),
      city: formData.get("city"),
      state: formData.get("state") || null,
      zipCode: formData.get("zipCode") || null,
      country: formData.get("country") || "Morocco",
      bloodType: formData.get("bloodType") || null,
      emergencyContact: formData.get("emergencyContact") || null,
      allergies,
      chronicDiseases,
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
          router.push("/patient/dashboard")
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

  const addAllergy = () => {
    if (newAllergy && !allergies.includes(newAllergy)) {
      setAllergies([...allergies, newAllergy])
      setNewAllergy("")
    }
  }

  const removeAllergy = (allergy) => {
    setAllergies(allergies.filter((a) => a !== allergy))
  }

  const addDisease = () => {
    if (newDisease && !chronicDiseases.includes(newDisease)) {
      setChronicDiseases([...chronicDiseases, newDisease])
      setNewDisease("")
    }
  }

  const removeDisease = (disease) => {
    setChronicDiseases(chronicDiseases.filter((d) => d !== disease))
  }

  if (!user || isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  if (!profile) return null

  const isProfileComplete = profile.firstName && profile.lastName && profile.phone && profile.dateOfBirth

  return (
    <div className="flex min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.myProfile || "My Profile"}</h1>
            <p className="text-muted-foreground">{t.personalInformation || "Manage your personal information"}</p>
          </div>

          {/* Account Status */}
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">{t.accountStatus || "Account Status"}</p>
                  <p className="text-lg font-semibold">{profile.email}</p>
                </div>
                <Badge variant={profile.accountStatus === "ACTIVE" ? "active" : "pending"}>
                  {profile.accountStatus}
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
                <CardTitle>{t.personalInformation || "Personal Information"}</CardTitle>
                <CardDescription>
                  {t.updatePersonalInfo || "Update your personal information"}
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="firstName">{t.firstName || "First Name"} *</Label>
                    <Input 
                      id="firstName" 
                      name="firstName" 
                      defaultValue={profile.firstName || ""} 
                      required 
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="lastName">{t.lastName || "Last Name"} *</Label>
                    <Input 
                      id="lastName" 
                      name="lastName" 
                      defaultValue={profile.lastName || ""} 
                      required 
                    />
                  </div>
                </div>

                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="phone">{t.phone || "Phone"} *</Label>
                    <Input
                      id="phone"
                      name="phone"
                      type="tel"
                      placeholder="+212 6 12 34 56 78"
                      defaultValue={profile.phone || ""}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="dateOfBirth">{t.dateOfBirth || "Date of Birth"} *</Label>
                    <Input
                      id="dateOfBirth"
                      name="dateOfBirth"
                      type="date"
                      defaultValue={profile.dateOfBirth || ""}
                      required
                    />
                  </div>
                </div>

                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="gender">{t.gender || "Gender"}</Label>
                    <Select name="gender" defaultValue={profile.gender || "male"}>
                      <SelectTrigger id="gender">
                        <SelectValue placeholder={t.selectGender || "Select gender"} />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="male">{t.male || "Male"}</SelectItem>
                        <SelectItem value="female">{t.female || "Female"}</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="bloodType">{t.bloodType || "Blood Type"}</Label>
                    <Select name="bloodType" defaultValue={profile.bloodType || "A+"}>
                      <SelectTrigger id="bloodType">
                        <SelectValue placeholder={t.selectBloodType || "Select blood type"} />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="A+">A+</SelectItem>
                        <SelectItem value="A-">A-</SelectItem>
                        <SelectItem value="B+">B+</SelectItem>
                        <SelectItem value="B-">B-</SelectItem>
                        <SelectItem value="AB+">AB+</SelectItem>
                        <SelectItem value="AB-">AB-</SelectItem>
                        <SelectItem value="O+">O+</SelectItem>
                        <SelectItem value="O-">O-</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="address">{t.address || "Address"}</Label>
                  <Input
                    id="address"
                    name="address"
                    placeholder="123 Rue Mohammed V"
                    defaultValue={profile.address || ""}
                  />
                </div>

                <div className="grid md:grid-cols-3 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="city">{t.city || "City"}</Label>
                    <Input 
                      id="city" 
                      name="city" 
                      placeholder="Casablanca" 
                      defaultValue={profile.city || ""} 
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="state">{t.state || "State/Region"}</Label>
                    <Input 
                      id="state" 
                      name="state" 
                      placeholder="Casablanca-Settat" 
                      defaultValue={profile.state || ""} 
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="zipCode">{t.zipCode || "Zip Code"}</Label>
                    <Input 
                      id="zipCode" 
                      name="zipCode" 
                      placeholder="20000" 
                      defaultValue={profile.zipCode || ""} 
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="country">{t.country || "Country"}</Label>
                  <Input 
                    id="country" 
                    name="country" 
                    placeholder="Morocco" 
                    defaultValue={profile.country || "Morocco"} 
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="emergencyContact">{t.emergencyContact || "Emergency Contact"}</Label>
                  <Input
                    id="emergencyContact"
                    name="emergencyContact"
                    type="tel"
                    placeholder="+212 6 98 76 54 32"
                    defaultValue={profile.emergencyContact || ""}
                  />
                </div>
              </CardContent>
            </Card>

            <Card className="mt-6">
              <CardHeader>
                <CardTitle>{t.medicalInformation || "Medical Information"}</CardTitle>
                <CardDescription>
                  {t.optionalMedicalInfo || "Optional: Add your medical information"}
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                {/* Allergies */}
                <div className="space-y-3">
                  <Label>{t.allergies || "Allergies"}</Label>
                  <div className="flex gap-2">
                    <Input
                      placeholder={t.enterAllergy || "Enter an allergy"}
                      value={newAllergy}
                      onChange={(e) => setNewAllergy(e.target.value)}
                      onKeyPress={(e) => e.key === "Enter" && (e.preventDefault(), addAllergy())}
                    />
                    <Button type="button" onClick={addAllergy} variant="outline">
                      {t.add || "Add"}
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {allergies.map((allergy, index) => (
                      <Badge key={index} variant="secondary" className="gap-1 px-3 py-1">
                        {allergy}
                        <button
                          type="button"
                          onClick={() => removeAllergy(allergy)}
                          className="ml-1 hover:text-destructive"
                        >
                          <X className="h-3 w-3" />
                        </button>
                      </Badge>
                    ))}
                  </div>
                </div>

                {/* Chronic Diseases */}
                <div className="space-y-3">
                  <Label>{t.chronicDiseases || "Chronic Diseases"}</Label>
                  <div className="flex gap-2">
                    <Input
                      placeholder={t.enterDisease || "Enter a chronic disease"}
                      value={newDisease}
                      onChange={(e) => setNewDisease(e.target.value)}
                      onKeyPress={(e) => e.key === "Enter" && (e.preventDefault(), addDisease())}
                    />
                    <Button type="button" onClick={addDisease} variant="outline">
                      {t.add || "Add"}
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {chronicDiseases.map((disease, index) => (
                      <Badge key={index} variant="secondary" className="gap-1 px-3 py-1">
                        {disease}
                        <button
                          type="button"
                          onClick={() => removeDisease(disease)}
                          className="ml-1 hover:text-destructive"
                        >
                          <X className="h-3 w-3" />
                        </button>
                      </Badge>
                    ))}
                  </div>
                </div>
              </CardContent>
            </Card>

            <div className="flex justify-end gap-4 mt-6">
              <Button 
                //onClick={handleSubmit}
                type="submit"
                size="lg" 
                className="bg-primary hover:bg-primary/90" 
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