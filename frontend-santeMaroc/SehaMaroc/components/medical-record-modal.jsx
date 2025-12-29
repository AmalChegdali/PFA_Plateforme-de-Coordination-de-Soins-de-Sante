"use client"
import { useState } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"

export function MedicalRecordModal({ isOpen, onClose, record, patient, mode, onSave }) {
  const [formData, setFormData] = useState(
    record || {
      recordType: "consultation",
      visitDate: new Date().toISOString().split("T")[0],
      diagnosis: "",
      symptoms: "",
      medications: "",
      notes: "",
    },
  )

  const recordTypes = [
    { value: "consultation", label: "Consultation" },
    { value: "lab", label: "Lab Test" },
    { value: "procedure", label: "Procedure" },
    { value: "vaccination", label: "Vaccination" },
    { value: "prescription", label: "Prescription" },
  ]

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = () => {
    onSave(formData)
    setFormData({
      recordType: "consultation",
      visitDate: new Date().toISOString().split("T")[0],
      diagnosis: "",
      symptoms: "",
      medications: "",
      notes: "",
    })
  }

  const isViewMode = mode === "view"

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {mode === "create"
              ? "Create Medical Record"
              : mode === "edit"
                ? "Edit Medical Record"
                : "View Medical Record"}
          </DialogTitle>
        </DialogHeader>

        {patient && (
          <div className="bg-secondary/30 p-3 rounded-lg mb-4">
            <p className="text-sm font-medium">
              {patient.profile?.firstName} {patient.profile?.lastName}
            </p>
            <p className="text-xs text-muted-foreground">{patient.email}</p>
          </div>
        )}

        <div className="space-y-4">
          {/* Record Type */}
          <div>
            <label className="text-sm font-medium">Record Type</label>
            <select
              name="recordType"
              value={formData.recordType}
              onChange={handleChange}
              disabled={isViewMode}
              className="w-full px-3 py-2 border rounded-lg bg-background text-foreground"
            >
              {recordTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>

          {/* Visit Date */}
          <div>
            <label className="text-sm font-medium">Visit Date</label>
            <input
              type="date"
              name="visitDate"
              value={formData.visitDate}
              onChange={handleChange}
              disabled={isViewMode}
              className="w-full px-3 py-2 border rounded-lg bg-background text-foreground"
            />
          </div>

          {/* Diagnosis */}
          <div>
            <label className="text-sm font-medium">Diagnosis</label>
            <input
              type="text"
              name="diagnosis"
              value={formData.diagnosis}
              onChange={handleChange}
              disabled={isViewMode}
              placeholder="Enter diagnosis"
              className="w-full px-3 py-2 border rounded-lg bg-background text-foreground placeholder-muted-foreground"
            />
          </div>

          {/* Symptoms */}
          <div>
            <label className="text-sm font-medium">Symptoms</label>
            <Textarea
              name="symptoms"
              value={formData.symptoms}
              onChange={handleChange}
              disabled={isViewMode}
              placeholder="Enter patient symptoms"
              className="min-h-20"
            />
          </div>

          {/* Medications */}
          <div>
            <label className="text-sm font-medium">Medications/Treatment</label>
            <Textarea
              name="medications"
              value={formData.medications}
              onChange={handleChange}
              disabled={isViewMode}
              placeholder="Enter prescribed medications or treatments"
              className="min-h-20"
            />
          </div>

          {/* Notes */}
          <div>
            <label className="text-sm font-medium">Additional Notes</label>
            <Textarea
              name="notes"
              value={formData.notes}
              onChange={handleChange}
              disabled={isViewMode}
              placeholder="Enter additional notes"
              className="min-h-24"
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            {isViewMode ? "Close" : "Cancel"}
          </Button>
          {!isViewMode && (
            <Button onClick={handleSubmit} className="bg-primary hover:bg-primary/90">
              {mode === "create" ? "Create Record" : "Update Record"}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
