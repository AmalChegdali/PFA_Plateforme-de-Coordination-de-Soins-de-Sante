"use client"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { LanguageSelector } from "@/components/language-selector"
import { Logo } from "@/components/logo"
import { useLanguage } from "@/contexts/LanguageContext"
import { Heart, Shield, UserCheck, ArrowRight } from "lucide-react"

export default function LandingPage() {
  const { lang, setLang, t } = useLanguage()

  return (
    <div className="min-h-screen" dir={lang === "ar" ? "rtl" : "ltr"}>
      {/* Header */}
      <header className="border-b bg-card sticky top-0 z-50 backdrop-blur-sm bg-card/80">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Logo size="md" />
            <LanguageSelector />
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="py-20 px-4 bg-gradient-to-br from-primary/5 via-accent/5 to-secondary/5 relative overflow-hidden">
        <div className="absolute top-10 right-10 opacity-5 pointer-events-none hidden lg:block">
          <Logo size="xl" showText={false} href={null} />
        </div>
        <div className="container mx-auto max-w-6xl relative z-10">
          <div className="text-center space-y-8">
            <h1 className="text-5xl md:text-6xl font-bold text-balance leading-tight">{t.heroTitle}</h1>
            <p className="text-xl text-muted-foreground max-w-3xl mx-auto text-pretty">{t.heroSubtitle}</p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center pt-4">
              <Link href="/auth/patient">
                <Button size="lg" className="w-full sm:w-auto gap-2 bg-primary hover:bg-primary/90 text-lg px-8 py-6">
                  {t.imPatient}
                  <ArrowRight className="h-5 w-5" />
                </Button>
              </Link>
              <Link href="/auth/provider">
                <Button
                  size="lg"
                  variant="outline"
                  className="w-full sm:w-auto gap-2 text-lg px-8 py-6 border-2 border-secondary text-secondary hover:bg-secondary hover:text-white bg-transparent"
                >
                  {t.imDoctor}
                  <ArrowRight className="h-5 w-5" />
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Advantages Section */}
      <section className="py-20 px-4">
        <div className="container mx-auto max-w-6xl">
          <div className="grid md:grid-cols-3 gap-8">
            <Card className="border-2 hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="w-12 h-12 bg-primary/10 rounded-xl flex items-center justify-center mb-4">
                  <Heart className="h-6 w-6 text-primary" />
                </div>
                <CardTitle className="text-2xl">{t.advantageTitle1}</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base leading-relaxed">{t.advantageDesc1}</CardDescription>
              </CardContent>
            </Card>

            <Card className="border-2 hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="w-12 h-12 bg-accent/10 rounded-xl flex items-center justify-center mb-4">
                  <Shield className="h-6 w-6 text-accent" />
                </div>
                <CardTitle className="text-2xl">{t.advantageTitle2}</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base leading-relaxed">{t.advantageDesc2}</CardDescription>
              </CardContent>
            </Card>

            <Card className="border-2 hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="w-12 h-12 bg-secondary/10 rounded-xl flex items-center justify-center mb-4">
                  <UserCheck className="h-6 w-6 text-secondary" />
                </div>
                <CardTitle className="text-2xl">{t.advantageTitle3}</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base leading-relaxed">{t.advantageDesc3}</CardDescription>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t bg-muted/30 py-8 px-4 mt-20">
        <div className="container mx-auto max-w-6xl">
          <div className="flex flex-col items-center gap-4">
            <Logo size="md" href="/" />
            <p className="text-sm text-muted-foreground">
              {t.copyright}
            </p>
          </div>
        </div>
      </footer>
    </div>
  )
}
