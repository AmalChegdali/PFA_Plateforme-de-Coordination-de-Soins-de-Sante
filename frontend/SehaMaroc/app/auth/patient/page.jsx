"use client"
import { useState } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { LanguageSelector } from "@/components/language-selector"
import { Logo } from "@/components/logo"
import { translations } from "@/lib/translations"
import { authenticateUser, registerPatient } from "@/lib/mock-data"
import { useToast } from "@/hooks/use-toast"
import { ArrowLeft } from "lucide-react"

export default function PatientAuthPage() {
  const [lang, setLang] = useState("en")
  const [isLoading, setIsLoading] = useState(false)
  const router = useRouter()
  const { toast } = useToast()
  const t = translations[lang]

  const handleLogin = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    const formData = new FormData(e.target)
    const email = formData.get("email")
    const password = formData.get("password")

    const user = authenticateUser(email, password)

    setTimeout(() => {
      if (user && user.role === "patient") {
        // Store user in session (in real app, use proper session management)
        sessionStorage.setItem("user", JSON.stringify(user))

        // Check if profile is complete
        if (!user.profile.profileComplete) {
          router.push("/patient/profile?complete=true")
        } else {
          router.push("/patient/dashboard")
        }
      } else {
        toast({
          title: "Error",
          description: "Invalid email or password",
          variant: "destructive",
        })
      }
      setIsLoading(false)
    }, 800)
  }

  const handleRegister = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    const formData = new FormData(e.target)
    const email = formData.get("email")
    const password = formData.get("password")

    const newUser = registerPatient(email, password)

    setTimeout(() => {
      sessionStorage.setItem("user", JSON.stringify(newUser))
      toast({
        title: "Success",
        description: "Account created successfully!",
      })
      router.push("/patient/profile?complete=true")
      setIsLoading(false)
    }, 800)
  }

  return (
    <div
      className={`min-h-screen bg-gradient-to-br from-primary/5 via-accent/5 to-secondary/5 ${lang === "ar" ? "rtl" : ""}`}
      dir={lang === "ar" ? "rtl" : "ltr"}
    >
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <Link href="/">
            <Button variant="ghost" className="gap-2">
              <ArrowLeft className="h-4 w-4" />
              Back
            </Button>
          </Link>
          <LanguageSelector currentLang={lang} onLangChange={setLang} />
        </div>

        <div className="flex flex-col items-center justify-center">
          {/* Logo */}
          <div className="mb-8">
            <Logo size="lg" href="/" />
          </div>

          {/* Auth Card */}
          <Card className="w-full max-w-md">
            <Tabs defaultValue="login" className="w-full">
              <CardHeader>
                <TabsList className="grid w-full grid-cols-2">
                  <TabsTrigger value="login">{t.login}</TabsTrigger>
                  <TabsTrigger value="register">{t.register}</TabsTrigger>
                </TabsList>
                <CardTitle className="text-2xl">{t.patientLogin}</CardTitle>
                <CardDescription>Enter your credentials to access your account</CardDescription>
              </CardHeader>

              <TabsContent value="login">
                <form onSubmit={handleLogin}>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="login-email">{t.email}</Label>
                      <Input id="login-email" name="email" type="email" placeholder="patient@sehamaroc.com" required />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="login-password">{t.password}</Label>
                      <Input id="login-password" name="password" type="password" required />
                    </div>
                  </CardContent>
                  <CardFooter>
                    <Button type="submit" className="w-full bg-primary hover:bg-primary/90" disabled={isLoading}>
                      {isLoading ? "Loading..." : t.login}
                    </Button>
                  </CardFooter>
                </form>
              </TabsContent>

              <TabsContent value="register">
                <form onSubmit={handleRegister}>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="register-email">{t.email}</Label>
                      <Input id="register-email" name="email" type="email" placeholder="you@example.com" required />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="register-password">{t.password}</Label>
                      <Input id="register-password" name="password" type="password" required />
                    </div>
                    <p className="text-sm text-muted-foreground">{t.completeProfileAfter}</p>
                  </CardContent>
                  <CardFooter>
                    <Button type="submit" className="w-full bg-primary hover:bg-primary/90" disabled={isLoading}>
                      {isLoading ? "Loading..." : t.register}
                    </Button>
                  </CardFooter>
                </form>
              </TabsContent>
            </Tabs>
          </Card>

          <p className="mt-4 text-sm text-muted-foreground">Demo credentials: patient@sehamaroc.com / password</p>
        </div>
      </div>
    </div>
  )
}
