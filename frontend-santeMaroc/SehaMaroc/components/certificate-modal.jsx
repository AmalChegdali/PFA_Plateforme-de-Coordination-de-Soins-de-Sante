"use client"

import { useState, useEffect } from "react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { translations } from "@/lib/translations"
import { useState as useStateHook } from "react"

export function CertificateModal({ isOpen, onClose, onSave, mode, patient, certificate }) {
  const [lang] = useStateHook("en")
  const t = translations[lang]
  const [formData, setFormData] = useState({
    diagnosis: "",
    content: "",
    issueDate: new Date().toISOString().split("T")[0],
    expirationDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split("T")[0],
    status: "active",
  })

  useEffect(() => {
    if (mode === "view" || mode === "edit") {
      setFormData({
        diagnosis: certificate?.diagnosis || "",
        content: certificate?.content || "",
        issueDate: certificate?.issueDate || new Date().toISOString().split("T")[0],
        expirationDate:
          certificate?.expirationDate || new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split("T")[0],
        status: certificate?.status || "active",
      })
    } else {
      setFormData({
        diagnosis: "",
        content: "",
        issueDate: new Date().toISOString().split("T")[0],
        expirationDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split("T")[0],
        status: "active",
      })
    }
  }, [isOpen, mode, certificate])

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = () => {
    if (!formData.diagnosis || !formData.content) {
      alert("Please fill in all required fields")
      return
    }
    onSave(formData)
  }

  const isReadOnly = mode === "view"

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>
            {mode === "create" ? "Create Certificate" : mode === "edit" ? "Edit Certificate" : "View Certificate"}
          </DialogTitle>
          {patient && (
            <DialogDescription>
              Patient: {patient.profile.firstName} {patient.profile.lastName}
            </DialogDescription>
          )}
        </DialogHeader>

        <div className="space-y-6 py-4">
          {/* Diagnosis */}
          <div className="space-y-2">
            <Label htmlFor="diagnosis">Diagnosis *</Label>
            <Input
              id="diagnosis"
              name="diagnosis"
              placeholder="e.g., Common Cold, Flu Vaccination"
              value={formData.diagnosis}
              onChange={handleInputChange}
              disabled={isReadOnly}
            />
          </div>

          {/* Certificate Content */}
          <div className="space-y-2">
            <Label htmlFor="content">Certificate Content *</Label>
            <Textarea
              id="content"
              name="content"
              placeholder="Enter the full certificate content or medical details..."
              value={formData.content}
              onChange={handleInputChange}
              disabled={isReadOnly}
              rows={5}
            />
          </div>

          {/* Dates */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="issueDate">Issue Date *</Label>
              <Input
                id="issueDate"
                name="issueDate"
                type="date"
                value={formData.issueDate}
                onChange={handleInputChange}
                disabled={isReadOnly}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="expirationDate">Expiration Date *</Label>
              <Input
                id="expirationDate"
                name="expirationDate"
                type="date"
                value={formData.expirationDate}
                onChange={handleInputChange}
                disabled={isReadOnly}
              />
            </div>
          </div>

          {/* Status */}
          <div className="space-y-2">
            <Label htmlFor="status">Status</Label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={handleInputChange}
              disabled={isReadOnly}
              className="w-full px-3 py-2 border border-input rounded-md bg-background text-sm"
            >
              <option value="active">Active</option>
              <option value="expired">Expired</option>
              <option value="revoked">Revoked</option>
            </select>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            {isReadOnly ? "Close" : "Cancel"}
          </Button>
          {!isReadOnly && (
            <Button onClick={handleSubmit} className="bg-primary hover:bg-primary/90">
              {mode === "create" ? "Create" : "Update"}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
