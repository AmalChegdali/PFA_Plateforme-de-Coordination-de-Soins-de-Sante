import { Geist, Inter } from "next/font/google"
import { Analytics } from "@vercel/analytics/next"
import "./globals.css"
import { Toaster } from "@/components/ui/toaster"
import { LanguageProvider } from "@/contexts/LanguageContext"
import { LanguageWrapper } from "@/components/LanguageWrapper"

const geist = Geist({ subsets: ["latin"] })
const inter = Inter({ subsets: ["latin"] })

export const metadata = {
  title: "SehaMaroc - Your health, our priority",
  description: "Modern healthcare management platform",
  generator: "v0.app",
  icons: {
    icon: "/logo.png",
  },
}

export default function RootLayout({ children }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={`font-sans antialiased`}>
        <LanguageProvider>
          <LanguageWrapper>
            {children}
            <Toaster />
            <Analytics />
          </LanguageWrapper>
        </LanguageProvider>
      </body>
    </html>
  )
}
