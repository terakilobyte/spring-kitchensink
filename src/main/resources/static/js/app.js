document.addEventListener("DOMContentLoaded", function () {
  const memberTableBody = document.querySelector("#memberTable tbody")
  const registrationForm = document.getElementById("regForm")
  const formErrorsDiv = document.getElementById("form-errors")
  const memberListErrorDiv = document.getElementById("member-list-error")
  const noMembersRow = document.getElementById("no-members-row")

  const API_BASE_URL = "/rest/members"

  async function fetchAndDisplayMembers() {
    hideError(memberListErrorDiv)
    memberTableBody.innerHTML = ""
    showNoMembersRow(false)

    try {
      const response = await fetch(API_BASE_URL)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const members = await response.json()

      if (members && members.length > 0) {
        members.forEach((member) => {
          const row = memberTableBody.insertRow()
          row.insertCell(0).textContent = member.id
          row.insertCell(1).textContent = member.name
          row.insertCell(2).textContent = member.email
          row.insertCell(3).textContent = member.phoneNumber
          // Add a link to the specific member's REST endpoint
          const restUrlCell = row.insertCell(4)
          const link = document.createElement("a")
          link.href = `${API_BASE_URL}/${member.id}`
          link.textContent = `${API_BASE_URL}/${member.id}`
          link.target = "_blank" // Open in new tab
          restUrlCell.appendChild(link)
        })
      } else {
        showNoMembersRow(true) // Show 'no members' row if list is empty
      }
    } catch (error) {
      console.error("Error fetching members:", error)
      showError(memberListErrorDiv, `Failed to load members: ${error.message}`)
      showNoMembersRow(true) // Show 'no members' row on error
    }
  }

  // --- Function to handle form submission ---
  async function handleRegistration(event) {
    event.preventDefault() // Prevent default HTML form submission
    hideError(formErrorsDiv) // Hide previous form errors
    clearInlineErrors() // Clear inline validation messages

    const formData = new FormData(registrationForm)
    const memberData = Object.fromEntries(formData.entries())

    // Basic client-side check (though server-side validation is primary)
    if (!memberData.name || !memberData.email || !memberData.phoneNumber) {
      showError(formErrorsDiv, "Please fill in all fields.")
      return
    }

    try {
      const response = await fetch(API_BASE_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(memberData),
      })

      if (response.ok) {
        // Status 201 Created
        registrationForm.reset() // Clear the form
        fetchAndDisplayMembers() // Refresh the member list
      } else {
        // Handle errors (Validation, Conflict, etc.)
        const errorData = await response.json()
        let errorMessage = `Registration failed (Status: ${response.status})`
        if (response.status === 400 && typeof errorData === "object") {
          // Validation errors
          errorMessage = "Please correct the errors below:"
          displayInlineErrors(errorData)
        } else if (response.status === 409 && errorData.error) {
          // Conflict (e.g., duplicate email)
          errorMessage = errorData.error
        } else if (errorData.error) {
          // Other server errors with an error message
          errorMessage = errorData.error
        } else if (typeof errorData === "string") {
          // Sometimes error might be plain text
          errorMessage = errorData
        }
        showError(formErrorsDiv, errorMessage)
      }
    } catch (error) {
      console.error("Error registering member:", error)
      showError(formErrorsDiv, `An network error occurred: ${error.message}`)
    }
  }

  // --- Helper functions for UI updates ---
  function showError(element, message) {
    element.textContent = message
    element.style.display = "block"
  }

  function hideError(element) {
    element.textContent = ""
    element.style.display = "none"
  }

  function displayInlineErrors(errors) {
    // Display validation errors next to the corresponding fields
    if (errors.name)
      document.getElementById("name-error").textContent = errors.name
    if (errors.email)
      document.getElementById("email-error").textContent = errors.email
    if (errors.phoneNumber)
      document.getElementById("phone-error").textContent = errors.phoneNumber
  }

  function clearInlineErrors() {
    document.getElementById("name-error").textContent = ""
    document.getElementById("email-error").textContent = ""
    document.getElementById("phone-error").textContent = ""
  }

  function showNoMembersRow(show) {
    noMembersRow.style.display = show ? "" : "none" // Show or hide the row
  }

  // --- Attach event listeners ---
  registrationForm.addEventListener("submit", handleRegistration)

  // --- Initial load ---
  fetchAndDisplayMembers()
})
