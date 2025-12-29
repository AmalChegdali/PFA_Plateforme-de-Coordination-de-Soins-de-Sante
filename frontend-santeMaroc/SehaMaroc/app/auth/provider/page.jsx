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
import { ArrowLeft, Loader2 } from "lucide-react"

const API_BASE_URL = "http://localhost:8082/api/auth"

export default function ProviderAuthPage() {
  const { lang, setLang, t } = useLanguage()
  const [isLoading, setIsLoading] = useState(false)
  const router = useRouter()
  const { toast } = useToast()

  const handleLogin = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    const formData = new FormData(e.target)
    const loginData = {
      email: formData.get("email"),
      password: formData.get("password"),
    }

    try {
      const response = await fetch(`${API_BASE_URL}/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(loginData),
      })

      const data = await response.json()

      if (response.ok) {
        // Store token and user data
        localStorage.setItem("token", data.token)
        localStorage.setItem("user", JSON.stringify({
          email: data.email,
          role: data.role,
        }))

        toast({
          title: t.success || "Success",
          description: data.message || t.loginSuccessful || "Login successful",
        })

        // Redirect to provider dashboard
        router.push("/provider/dashboard")
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

  // const handleRegister = async (e) => {
  //   e.preventDefault()
  //   setIsLoading(true)

  //   const formData = new FormData(e.target)
  //   const providerData = {
  //     email: formData.get("email"),
  //     password: formData.get("password"),
  //     firstName: formData.get("firstName"),
  //     lastName: formData.get("lastName"),
  //     specialization: formData.get("specialization"),
  //     phone: formData.get("phone"),
  //     licenseNumber: formData.get("licenseNumber"),
  //   }

  //   try {
  //     const response = await fetch(`${API_BASE_URL}/register`, {
  //       method: "POST",
  //       headers: {
  //         "Content-Type": "application/json",
  //       },
  //       body: JSON.stringify(providerData),
  //     })

  //     const data = await response.json()

  //     if (response.ok) {
  //       // Store token and user data
  //       localStorage.setItem("token", data.token)
  //       localStorage.setItem("user", JSON.stringify({
  //         email: data.email,
  //         role: data.role,
  //       }))

  //       toast({
  //         title: t.success || "Success",
  //         description: t.accountCreated || "Account created successfully",
  //       })

  //       router.push("/provider/dashboard")
  //     } else {
  //       toast({
  //         title: t.error || "Error",
  //         description: data.message || t.registrationFailed || "Registration failed",
  //         variant: "destructive",
  //       })
  //     }
  //   } catch (error) {
  //     console.error("Registration error:", error)
  //     toast({
  //       title: t.error || "Error",
  //       description: t.connectionError || "Connection error. Please try again.",
  //       variant: "destructive",
  //     })
  //   } finally {
  //     setIsLoading(false)
  //   }
  // }

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
          <Card className="w-full max-w-2xl">
            <Tabs defaultValue="login" className="w-full">
              <CardHeader>
                <TabsList className="grid w-full grid-cols-1">
                  <TabsTrigger value="login">{t.login || "Login"}</TabsTrigger>
                  {/* <TabsTrigger value="register">{t.register || "Register"}</TabsTrigger> */}
                </TabsList>
                <CardTitle className="text-2xl">{t.providerLogin || "Provider Login"}</CardTitle>
                <CardDescription>{t.accessProviderAccount || "Access your provider account"}</CardDescription>
              </CardHeader>

              <TabsContent value="login">
                <form onSubmit={handleLogin}>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="login-email">{t.email || "Email"}</Label>
                      <Input 
                        id="login-email" 
                        name="email" 
                        type="email" 
                        placeholder="provider@example.com" 
                        required 
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="login-password">{t.password || "Password"}</Label>
                      <Input 
                        id="login-password" 
                        name="password" 
                        type="password" 
                        required 
                      />
                    </div>
                  </CardContent>
                  <CardFooter>
                    <Button 
                      type="submit" 
                      className="w-full bg-secondary hover:bg-secondary/90" 
                      disabled={isLoading}
                    >
                      {isLoading ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          {t.loading || "Loading..."}
                        </>
                      ) : (
                        t.login || "Login"
                      )}
                    </Button>
                  </CardFooter>
                </form>
              </TabsContent>

              {/* <TabsContent value="register">
                <form onSubmit={handleRegister}>
                  <CardContent className="space-y-4">
                    <div className="grid md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="firstName">{t.firstName || "First Name"}</Label>
                        <Input id="firstName" name="firstName" required />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="lastName">{t.lastName || "Last Name"}</Label>
                        <Input id="lastName" name="lastName" required />
                      </div>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="specialization">{t.specialization || "Specialization"}</Label>
                      <Input id="specialization" name="specialization" placeholder="Cardiology" required />
                    </div>
                    <div className="grid md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="phone">{t.phone || "Phone"}</Label>
                        <Input id="phone" name="phone" type="tel" placeholder="+212 6 12 34 56 78" required />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="licenseNumber">{t.licenseNumber || "License Number"}</Label>
                        <Input id="licenseNumber" name="licenseNumber" placeholder="MD-12345" required />
                      </div>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="register-email">{t.email || "Email"}</Label>
                      <Input id="register-email" name="email" type="email" placeholder="you@example.com" required />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="register-password">{t.password || "Password"}</Label>
                      <Input id="register-password" name="password" type="password" required />
                    </div>
                  </CardContent>
                  <CardFooter>
                    <Button 
                      type="submit" 
                      className="w-full bg-secondary hover:bg-secondary/90" 
                      disabled={isLoading}
                    >
                      {isLoading ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          {t.loading || "Loading..."}
                        </>
                      ) : (
                        t.register || "Register"
                      )}
                    </Button>
                  </CardFooter>
                </form>
              </TabsContent> */}
            </Tabs>
          </Card>

          <p className="mt-4 text-sm text-muted-foreground">
            {t.demoCredentialsProvider || "Demo: provider1@mail.com / provider1@mail.com"}
          </p>
        </div>
      </div>
    </div>
  )
}