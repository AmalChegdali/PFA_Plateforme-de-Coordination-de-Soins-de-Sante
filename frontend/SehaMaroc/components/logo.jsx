"use client"
import Image from "next/image"
import Link from "next/link"
import { cn } from "@/lib/utils"

export function Logo({ size = "md", showText = true, href = "/", className }) {
  const sizes = {
    sm: { image: 32, text: "text-base" },
    md: { image: 40, text: "text-xl" },
    lg: { image: 60, text: "text-3xl" },
    xl: { image: 80, text: "text-4xl" },
  }

  const { image, text } = sizes[size]

  const content = (
    <div className={cn("flex items-center gap-3 group", className)}>
      <div className="relative transition-transform group-hover:scale-110">
        <Image
          src="/logo-icon.svg"
          alt="SehaMaroc Logo"
          width={image}
          height={image}
          className="object-contain drop-shadow-md"
        />
      </div>
      {showText && (
        <span className={cn("font-bold tracking-tight", text)}>
          <span className="text-secondary">Seha</span>
          <span className="text-primary">Maroc</span>
        </span>
      )}
    </div>
  )

  if (href) {
    return (
      <Link href={href} className="inline-block">
        {content}
      </Link>
    )
  }

  return content
}
