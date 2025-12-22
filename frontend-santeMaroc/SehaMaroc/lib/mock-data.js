// Mock data for demonstration purposes
// In a real app, this would come from a database

export const mockUsers = [
  {
    id: "1",
    email: "patient@sehamaroc.com",
    password: "password",
    role: "patient",
    profile: {
      firstName: "Ahmed",
      lastName: "Benali",
      phone: "+212 6 12 34 56 78",
      dateOfBirth: "1990-05-15",
      gender: "male",
      address: "123 Rue Mohammed V",
      city: "Casablanca",
      allergies: ["Penicillin", "Pollen"],
      chronicDiseases: ["Diabetes Type 2"],
      emergencyContact: "+212 6 98 76 54 32",
      bloodType: "A+",
      profileComplete: true,
      status: "active",
    },
  },
  {
    id: "2",
    email: "newpatient@sehamaroc.com",
    password: "password",
    role: "patient",
    profile: {
      profileComplete: false,
      status: "pending",
    },
  },
  {
    id: "3",
    email: "doctor@sehamaroc.com",
    password: "password",
    role: "provider",
    profile: {
      firstName: "Dr. Fatima",
      lastName: "Alami",
      specialization: "Cardiology",
      phone: "+212 6 11 22 33 44",
      licenseNumber: "MD-12345",
      address: "456 Boulevard Zerktouni",
      city: "Casablanca",
      profilePicture: null,
    },
  },
]

export const mockRequests = [
  {
    id: "1",
    patientId: "1",
    patientName: "Ahmed Benali",
    type: "consultation",
    subject: "Chest pain consultation",
    description: "I have been experiencing chest pain for the past two days",
    status: "pending",
    priority: "high",
    preferredDate: "2024-12-20",
    doctorId: null,
    createdAt: "2024-12-13T10:00:00Z",
  },
  {
    id: "2",
    patientId: "1",
    patientName: "Ahmed Benali",
    type: "prescription",
    subject: "Diabetes medication refill",
    description: "Need refill for my diabetes medication",
    status: "in_progress",
    priority: "medium",
    preferredDate: "2024-12-18",
    doctorId: "3",
    createdAt: "2024-12-10T14:30:00Z",
  },
  {
    id: "3",
    patientId: "1",
    patientName: "Ahmed Benali",
    type: "certificate",
    subject: "Medical certificate for work",
    description: "Need medical certificate for sick leave",
    status: "completed",
    priority: "low",
    preferredDate: "2024-12-15",
    doctorId: "3",
    createdAt: "2024-12-08T09:00:00Z",
    response: "Certificate has been issued",
    respondedAt: "2024-12-09T11:00:00Z",
  },
]

export const mockNotifications = [
  {
    id: "1",
    patientId: "1",
    message: "Dr. Alami has responded to your prescription request",
    doctorName: "Dr. Fatima Alami",
    date: "2024-12-12T16:00:00Z",
    read: false,
  },
  {
    id: "2",
    patientId: "1",
    message: "Your medical certificate is ready for download",
    doctorName: "Dr. Fatima Alami",
    date: "2024-12-09T11:00:00Z",
    read: true,
  },
]

export const mockCertificates = [
  {
    id: "1",
    patientId: "1",
    patientName: "Ahmed Benali",
    doctorId: "3",
    diagnosis: "Medical Certificate for Sick Leave",
    content:
      "This is to certify that Mr. Ahmed Benali was under my care from December 8-10, 2024, and is medically unfit for work during this period.",
    issueDate: "2024-12-09",
    expirationDate: "2024-12-16",
    status: "active",
    createdAt: "2024-12-09T11:00:00Z",
  },
  {
    id: "2",
    patientId: "1",
    patientName: "Ahmed Benali",
    doctorId: "3",
    diagnosis: "Vaccination Certificate",
    content:
      "This certifies that Mr. Ahmed Benali has received the flu vaccination as per WHO guidelines. Vaccination date: December 5, 2024.",
    issueDate: "2024-12-05",
    expirationDate: "2025-12-05",
    status: "active",
    createdAt: "2024-12-05T09:30:00Z",
  },
]

// Helper functions
export function authenticateUser(email, password) {
  const user = mockUsers.find((u) => u.email === email && u.password === password)
  if (user) {
    const { password: _, ...userWithoutPassword } = user
    return userWithoutPassword
  }
  return null
}

export function registerPatient(email, password) {
  const newUser = {
    id: `patient-${Date.now()}`,
    email,
    password,
    role: "patient",
    profile: {
      profileComplete: false,
      status: "pending",
    },
  }
  mockUsers.push(newUser)
  const { password: _, ...userWithoutPassword } = newUser
  return userWithoutPassword
}

export function registerProvider(data) {
  const newUser = {
    id: `provider-${Date.now()}`,
    email: data.email,
    password: data.password,
    role: "provider",
    profile: {
      firstName: data.firstName,
      lastName: data.lastName,
      specialization: data.specialization,
      phone: data.phone,
      licenseNumber: data.licenseNumber,
      profilePicture: null,
    },
  }
  mockUsers.push(newUser)
  const { password: _, ...userWithoutPassword } = newUser
  return userWithoutPassword
}
