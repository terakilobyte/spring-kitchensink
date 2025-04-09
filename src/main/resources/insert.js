// You can run this script to insert a new member into the database using
// mongosh.
db.getSiblingDB("kitchensink").members.insertOne({
  name: "John Smith",
  email: "john.smith@mailinator.com",
  phone_number: "2125551212",
})
