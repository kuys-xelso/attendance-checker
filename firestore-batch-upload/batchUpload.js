const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(require('./service-account.json')),
});

const db = admin.firestore();

async function batchUploadStudents() {
  // Array of student data to be uploaded
  const students = [

              {
                 "course": "CS",
                 "firstname": "Isabella",
                 "lastname": "Garcia",
                 "middle_initial": "P",
                 "section": "A",
                 "student_id": "443501",
                 "user_role": "Student",
                 "year_level": "4"
               },
               {
                 "course": "CS",
                 "firstname": "Noah",
                 "lastname": "Hernandez",
                 "middle_initial": "Q",
                 "section": "A",
                 "student_id": "443502",
                 "user_role": "Student",
                 "year_level": "4"
               },
               {
                 "course": "CS",
                 "firstname": "Emma",
                 "lastname": "Martinez",
                 "middle_initial": "R",
                 "section": "A",
                 "student_id": "443503",
                 "user_role": "Student",
                 "year_level": "4"
               },
               {
                 "course": "CS",
                 "firstname": "Lucas",
                 "lastname": "Rodriguez",
                 "middle_initial": "S",
                 "section": "A",
                 "student_id": "443504",
                 "user_role": "Student",
                 "year_level": "4"
               },
               {
                 "course": "CS",
                 "firstname": "Sophia",
                 "lastname": "Lopez",
                 "middle_initial": "T",
                 "section": "A",
                 "student_id": "443505",
                 "user_role": "Student",
                 "year_level": "4"
               }
               , {
                    "course": "CS",
                    "firstname": "Mason",
                    "lastname": "Torres",
                    "middle_initial": "U",
                    "section": "B",
                    "student_id": "443506",
                    "user_role": "Student",
                    "year_level": "4"
                  },
                  {
                    "course": "CS",
                    "firstname": "Mia",
                    "lastname": "Sanchez",
                    "middle_initial": "V",
                    "section": "B",
                    "student_id": "443507",
                    "user_role": "Student",
                    "year_level": "4"
                  },
                  {
                    "course": "CS",
                    "firstname": "Ethan",
                    "lastname": "Perez",
                    "middle_initial": "W",
                    "section": "B",
                    "student_id": "443508",
                    "user_role": "Student",
                    "year_level": "4"
                  },
                  {
                    "course": "CS",
                    "firstname": "Grace",
                    "lastname": "Ramirez",
                    "middle_initial": "X",
                    "section": "B",
                    "student_id": "443509",
                    "user_role": "Student",
                    "year_level": "4"
                  },
                  {
                    "course": "CS",
                    "firstname": "Jacob",
                    "lastname": "Reyes",
                    "middle_initial": "Y",
                    "section": "B",
                    "student_id": "443510",
                    "user_role": "Student",
                    "year_level": "4"
                  },
                   {
                      "course": "CS",
                      "firstname": "Liam",
                      "lastname": "Gonzalez",
                      "middle_initial": "Z",
                      "section": "C",
                      "student_id": "443511",
                      "user_role": "Student",
                      "year_level": "4"
                    },
                    {
                      "course": "CS",
                      "firstname": "Olivia",
                      "lastname": "Cruz",
                      "middle_initial": "A",
                      "section": "C",
                      "student_id": "443512",
                      "user_role": "Student",
                      "year_level": "4"
                    },
                    {
                      "course": "CS",
                      "firstname": "Aiden",
                      "lastname": "Mendoza",
                      "middle_initial": "B",
                      "section": "C",
                      "student_id": "443513",
                      "user_role": "Student",
                      "year_level": "4"
                    },
                    {
                      "course": "CS",
                      "firstname": "Aria",
                      "lastname": "Flores",
                      "middle_initial": "C",
                      "section": "C",
                      "student_id": "443514",
                      "user_role": "Student",
                      "year_level": "4"
                    },
                    {
                      "course": "CS",
                      "firstname": "Benjamin",
                      "lastname": "Diaz",
                      "middle_initial": "D",
                      "section": "C",
                      "student_id": "443515",
                      "user_role": "Student",
                      "year_level": "4"
                    }

                   ]
;


  const collectionRef = db.collection('users'); // Name of your Firestore collection
  const batch = db.batch();

  students.forEach((student) => {
    const docRef = collectionRef.doc(); // Automatically generate a document ID
    batch.set(docRef, student);
  });

  try {
    await batch.commit();
    console.log('Batch upload successful!');
  } catch (error) {
    console.error('Error during batch upload:', error);
  }
}

// Run the function
batchUploadStudents();