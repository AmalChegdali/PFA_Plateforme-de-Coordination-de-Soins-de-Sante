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
import { useLanguage } from "@/contexts/LanguageContext"
import { useToast } from "@/hooks/use-toast"
import { ArrowLeft } from "lucide-react"

const API_BASE_URL = "http://localhost:8081/api/auth"

export default function PatientAuthPage() {
  const { lang, setLang, t, mounted } = useLanguage()
  const [isLoading, setIsLoading] = useState(false)
  const [loginEmail, setLoginEmail] = useState("")
  const [loginPassword, setLoginPassword] = useState("")
  const [registerEmail, setRegisterEmail] = useState("")
  const [registerPassword, setRegisterPassword] = useState("")
  const router = useRouter()
  const { toast } = useToast()

  if (!mounted) {
    return null
  }

  const handleLogin = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    try {
      const response = await fetch(`${API_BASE_URL}/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ 
          email: loginEmail, 
          password: loginPassword 
        }),
      })

      const data = await response.json()

      if (response.ok) {
        // Store authentication data
        localStorage.setItem("token", data.token)
        localStorage.setItem("user", JSON.stringify({
          email: data.email,
          role: data.role,
          accountStatus: data.accountStatus,
          canAccessMedicalHistory: data.canAccessMedicalHistory
        }))

        toast({
          title: t.success || "Success",
          description: t.loginSuccess || "Login successful",
        })

        // Redirect to dashboard
        router.push("/patient/dashboard")
      } else {
        toast({
          title: t.error || "Error",
          description: data.message || t.invalidCredentials || "Invalid credentials",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Login error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error. Please try again.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleRegister = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    try {
      const response = await fetch(`${API_BASE_URL}/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ 
          email: registerEmail, 
          password: registerPassword 
        }),
      })

      const data = await response.json()

      if (response.ok) {
        // Store authentication data
        localStorage.setItem("token", data.token)
        localStorage.setItem("user", JSON.stringify({
          email: data.email,
          role: data.role,
          accountStatus: data.accountStatus,
          canAccessMedicalHistory: data.canAccessMedicalHistory,
          isNewUser: true // Flag to indicate this is a new registration
        }))

        toast({
          title: t.success || "Success",
          description: data.message || t.accountCreated || "Account created successfully",
        })

        // Redirect to profile completion page
        router.push("/patient/profile?complete=true")
      } else {
        toast({
          title: t.error || "Error",
          description: data.message || t.registrationFailed || "Registration failed",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Registration error:", error)
      toast({
        title: t.error || "Error",
        description: t.connectionError || "Connection error. Please try again.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div
      className="min-h-screen bg-gradient-to-br from-primary/5 via-accent/5 to-secondary/5"
      dir={lang === "ar" ? "rtl" : "ltr"}
    >
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <Link href="/">
            <Button variant="ghost" className="gap-2">
              <ArrowLeft className="h-4 w-4" />
              {t.back || "Back"}
            </Button>
          </Link>
          <LanguageSelector />
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
                  <TabsTrigger value="login">{t.login || "Login"}</TabsTrigger>
                  <TabsTrigger value="register">{t.register || "Register"}</TabsTrigger>
                </TabsList>
                <CardTitle className="text-2xl">{t.patientLogin || "Patient Portal"}</CardTitle>
                <CardDescription>{t.enterCredentials || "Enter your credentials"}</CardDescription>
              </CardHeader>

              <TabsContent value="login">
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="login-email">{t.email || "Email"}</Label>
                    <Input 
                      id="login-email" 
                      type="email" 
                      placeholder="patient@email.com" 
                      value={loginEmail}
                      onChange={(e) => setLoginEmail(e.target.value)}
                      required 
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="login-password">{t.password || "Password"}</Label>
                    <Input 
                      id="login-password" 
                      type="password"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      required 
                    />
                  </div>
                </CardContent>
                <CardFooter>
                  <Button 
                    onClick={handleLogin}
                    className="w-full bg-primary hover:bg-primary/90" 
                    disabled={isLoading}
                  >
                    {isLoading ? (t.loading || "Loading...") : (t.login || "Login")}
                  </Button>
                </CardFooter>
              </TabsContent>

              <TabsContent value="register">
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="register-email">{t.email || "Email"}</Label>
                    <Input 
                      id="register-email" 
                      type="email" 
                      placeholder="you@example.com"
                      value={registerEmail}
                      onChange={(e) => setRegisterEmail(e.target.value)}
                      required 
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="register-password">{t.password || "Password"}</Label>
                    <Input 
                      id="register-password" 
                      type="password"
                      value={registerPassword}
                      onChange={(e) => setRegisterPassword(e.target.value)}
                      required 
                    />
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {t.completeProfileAfter || "You can complete your profile after registration"}
                  </p>
                </CardContent>
                <CardFooter>
                  <Button 
                    onClick={handleRegister}
                    className="w-full bg-primary hover:bg-primary/90" 
                    disabled={isLoading}
                  >
                    {isLoading ? (t.loading || "Loading...") : (t.register || "Register")}
                  </Button>
                </CardFooter>
              </TabsContent>
            </Tabs>
          </Card>
        </div>
      </div>
    </div>
  )
}