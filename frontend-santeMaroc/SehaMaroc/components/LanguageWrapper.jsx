"use client"
import { useLanguage } from "@/contexts/LanguageContext"
import { useEffect } from "react"

export function LanguageWrapper({ children }) {
  const { lang } = useLanguage()

  useEffect(() => {
    // Mettre à jour l'attribut dir et lang sur l'élément html
    document.documentElement.setAttribute("dir", lang === "ar" ? "rtl" : "ltr")
    document.documentElement.setAttribute("lang", lang)
  }, [lang])

  return <>{children}</>
}

