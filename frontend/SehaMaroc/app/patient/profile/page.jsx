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
import { translations } from "@/lib/translations"
import { AlertCircle, X } from "lucide-react"

export default function PatientProfile() {
  const [lang, setLang] = useState("en")
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [allergies, setAllergies] = useState([])
  const [chronicDiseases, setChronicDiseases] = useState([])
  const [newAllergy, setNewAllergy] = useState("")
  const [newDisease, setNewDisease] = useState("")
  const router = useRouter()
  const searchParams = useSearchParams()
  const { toast } = useToast()
  const t = translations[lang]

  const shouldComplete = searchParams.get("complete") === "true"

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
    setAllergies(parsedUser.profile.allergies || [])
    setChronicDiseases(parsedUser.profile.chronicDiseases || [])
  }, [router])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    const formData = new FormData(e.target)
    const updatedProfile = {
      ...user.profile,
      firstName: formData.get("firstName"),
      lastName: formData.get("lastName"),
      phone: formData.get("phone"),
      dateOfBirth: formData.get("dateOfBirth"),
      gender: formData.get("gender"),
      address: formData.get("address"),
      city: formData.get("city"),
      bloodType: formData.get("bloodType"),
      emergencyContact: formData.get("emergencyContact"),
      allergies,
      chronicDiseases,
      profileComplete: true,
      status: "active",
    }

    setTimeout(() => {
      const updatedUser = { ...user, profile: updatedProfile }
      sessionStorage.setItem("user", JSON.stringify(updatedUser))
      setUser(updatedUser)
      toast({
        title: "Success",
        description: "Profile updated successfully!",
      })
      setIsLoading(false)
      if (shouldComplete) {
        router.push("/patient/dashboard")
      }
    }, 800)
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

  if (!user) return null

  return (
    <div className={`flex min-h-screen ${lang === "ar" ? "rtl" : ""}`} dir={lang === "ar" ? "rtl" : "ltr"}>
      <PatientSidebar user={user} lang={lang} onLangChange={setLang} />

      <main className="flex-1 p-6 lg:p-8 overflow-auto">
        <div className="max-w-4xl mx-auto space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-3xl font-bold text-balance">{t.myProfile}</h1>
            <p className="text-muted-foreground">Manage your personal and medical information</p>
          </div>

          {/* Incomplete Profile Alert */}
          {!user.profile.profileComplete && (
            <Alert className="border-2 border-primary/50 bg-primary/5">
              <AlertCircle className="h-5 w-5 text-primary" />
              <AlertTitle className="text-lg font-semibold">{t.completeProfile}</AlertTitle>
              <AlertDescription>{t.completeProfileMessage}</AlertDescription>
            </Alert>
          )}

          {/* Profile Form */}
          <form onSubmit={handleSubmit}>
            <Card>
              <CardHeader>
                <CardTitle>Personal Information</CardTitle>
                <CardDescription>Update your personal details</CardDescription>
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

                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="phone">{t.phone}</Label>
                    <Input
                      id="phone"
                      name="phone"
                      type="tel"
                      placeholder="+212 6 12 34 56 78"
                      defaultValue={user.profile.phone}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="dateOfBirth">Date of Birth</Label>
                    <Input
                      id="dateOfBirth"
                      name="dateOfBirth"
                      type="date"
                      defaultValue={user.profile.dateOfBirth}
                      required
                    />
                  </div>
                </div>

                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="gender">Gender</Label>
                    <Select name="gender" defaultValue={user.profile.gender || "male"}>
                      <SelectTrigger id="gender">
                        <SelectValue placeholder="Select gender" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="male">Male</SelectItem>
                        <SelectItem value="female">Female</SelectItem>
                        <SelectItem value="other">Other</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="bloodType">Blood Type</Label>
                    <Select name="bloodType" defaultValue={user.profile.bloodType || "A+"}>
                      <SelectTrigger id="bloodType">
                        <SelectValue placeholder="Select blood type" />
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
                  <Label htmlFor="address">Address</Label>
                  <Input
                    id="address"
                    name="address"
                    placeholder="123 Rue Mohammed V"
                    defaultValue={user.profile.address}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="city">City</Label>
                  <Input id="city" name="city" placeholder="Casablanca" defaultValue={user.profile.city} required />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="emergencyContact">Emergency Contact</Label>
                  <Input
                    id="emergencyContact"
                    name="emergencyContact"
                    type="tel"
                    placeholder="+212 6 98 76 54 32"
                    defaultValue={user.profile.emergencyContact}
                    required
                  />
                </div>
              </CardContent>
            </Card>

            <Card className="mt-6">
              <CardHeader>
                <CardTitle>Medical Information</CardTitle>
                <CardDescription>Add your allergies and chronic diseases</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                {/* Allergies */}
                <div className="space-y-3">
                  <Label>Allergies</Label>
                  <div className="flex gap-2">
                    <Input
                      placeholder="Add allergy"
                      value={newAllergy}
                      onChange={(e) => setNewAllergy(e.target.value)}
                      onKeyPress={(e) => e.key === "Enter" && (e.preventDefault(), addAllergy())}
                    />
                    <Button type="button" onClick={addAllergy} variant="outline">
                      Add
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {allergies.map((allergy) => (
                      <Badge key={allergy} variant="secondary" className="gap-1 px-3 py-1">
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
                  <Label>Chronic Diseases</Label>
                  <div className="flex gap-2">
                    <Input
                      placeholder="Add chronic disease"
                      value={newDisease}
                      onChange={(e) => setNewDisease(e.target.value)}
                      onKeyPress={(e) => e.key === "Enter" && (e.preventDefault(), addDisease())}
                    />
                    <Button type="button" onClick={addDisease} variant="outline">
                      Add
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {chronicDiseases.map((disease) => (
                      <Badge key={disease} variant="secondary" className="gap-1 px-3 py-1">
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
              <Button type="submit" size="lg" className="bg-primary hover:bg-primary/90" disabled={isLoading}>
                {isLoading ? "Saving..." : t.save}
              </Button>
            </div>
          </form>
        </div>
      </main>
    </div>
  )
}
