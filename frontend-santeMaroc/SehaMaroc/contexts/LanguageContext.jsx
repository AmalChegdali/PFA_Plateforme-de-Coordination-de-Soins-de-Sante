"use client"
import { createContext, useContext, useState, useEffect, useMemo, useCallback } from "react"
import { translations } from "@/lib/translations"

const LanguageContext = createContext()

export function LanguageProvider({ children }) {
  // Initialiser avec "en" par défaut, puis mettre à jour depuis localStorage dans useEffect
  const [lang, setLangState] = useState("en")
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    // Récupérer la langue depuis localStorage au montage
    if (typeof window !== "undefined") {
      const savedLang = localStorage.getItem("language") || "en"
      setLangState(savedLang)
      setMounted(true)
    }
  }, [])

  const changeLanguage = useCallback((newLang) => {
    setLangState(newLang)
    if (typeof window !== "undefined") {
      localStorage.setItem("language", newLang)
    }
  }, [])

  // Utiliser useMemo pour que les traductions soient réactives
  const t = useMemo(() => {
    return translations[lang] || translations.en
  }, [lang])

  const value = useMemo(() => ({
    lang,
    setLang: changeLanguage,
    t,
    mounted
  }), [lang, changeLanguage, t, mounted])

  return (
    <LanguageContext.Provider value={value}>
      {children}
    </LanguageContext.Provider>
  )
}

export function useLanguage() {
  const context = useContext(LanguageContext)
  if (!context) {
    throw new Error("useLanguage must be used within a LanguageProvider")
  }
  return context
}

