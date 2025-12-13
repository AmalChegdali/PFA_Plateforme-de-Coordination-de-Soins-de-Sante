"use client"
import { useState } from "react"
import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { LanguageSelector } from "@/components/language-selector"
import { Logo } from "@/components/logo"
import { translations } from "@/lib/translations"
import { LayoutDashboard, User, FileText, History, Bell, LogOut, Menu, X } from "lucide-react"

export function PatientSidebar({ user, lang, onLangChange }) {
  const [isOpen, setIsOpen] = useState(false)
  const pathname = usePathname()
  const router = useRouter()
  const t = translations[lang]

  const navigation = [
    { name: t.dashboard, href: "/patient/dashboard", icon: LayoutDashboard },
    { name: t.myProfile, href: "/patient/profile", icon: User },
    { name: t.myRequests, href: "/patient/requests", icon: FileText },
    { name: t.medicalHistory, href: "/patient/history", icon: History },
    { name: t.notifications, href: "/patient/notifications", icon: Bell },
  ]

  const handleLogout = () => {
    sessionStorage.removeItem("user")
    router.push("/")
  }

  const getInitials = (firstName, lastName) => {
    return `${firstName?.[0] || "U"}${lastName?.[0] || "U"}`.toUpperCase()
  }

  return (
    <>
      {/* Mobile Menu Button */}
      <div className="lg:hidden fixed top-4 left-4 z-50">
        <Button variant="outline" size="icon" onClick={() => setIsOpen(!isOpen)}>
          {isOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </Button>
      </div>

      {/* Overlay */}
      {isOpen && <div className="lg:hidden fixed inset-0 bg-black/50 z-40" onClick={() => setIsOpen(false)} />}

      {/* Sidebar */}
      <aside
        className={cn(
          "fixed lg:sticky top-0 left-0 z-40 h-screen bg-sidebar border-r border-sidebar-border flex flex-col transition-transform lg:translate-x-0",
          isOpen ? "translate-x-0" : "-translate-x-full",
          "w-64",
        )}
      >
        {/* Logo */}
        <div className="p-6 border-b border-sidebar-border">
          <Logo size="md" href="/patient/dashboard" />
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
          {navigation.map((item) => {
            const isActive = pathname === item.href
            return (
              <Link
                key={item.name}
                href={item.href}
                onClick={() => setIsOpen(false)}
                className={cn(
                  "flex items-center gap-3 px-4 py-3 rounded-lg transition-colors",
                  isActive
                    ? "bg-sidebar-primary text-sidebar-primary-foreground"
                    : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                )}
              >
                <item.icon className="h-5 w-5" />
                <span className="font-medium">{item.name}</span>
              </Link>
            )
          })}
        </nav>

        {/* User Profile */}
        <div className="p-4 border-t border-sidebar-border space-y-3">
          <div className="flex items-center gap-3">
            <Avatar>
              <AvatarFallback className="bg-primary text-primary-foreground">
                {getInitials(user?.profile?.firstName, user?.profile?.lastName)}
              </AvatarFallback>
            </Avatar>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-sidebar-foreground truncate">
                {user?.profile?.firstName && user?.profile?.lastName
                  ? `${user.profile.firstName} ${user.profile.lastName}`
                  : user?.email}
              </p>
              <p className="text-xs text-muted-foreground">Patient</p>
            </div>
          </div>
          <div className="flex gap-2">
            <Button variant="ghost" size="sm" onClick={handleLogout} className="flex-1 gap-2">
              <LogOut className="h-4 w-4" />
              {t.logout}
            </Button>
            <LanguageSelector currentLang={lang} onLangChange={onLangChange} />
          </div>
        </div>
      </aside>
    </>
  )
}
