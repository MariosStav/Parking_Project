package com.example.parkhonolulu.ui.login;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.parkhonolulu.HomePage;
import com.example.parkhonolulu.R;
import com.example.parkhonolulu.User;
import com.example.parkhonolulu.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class CombinedSignUpAndLoginTest {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    // Use a unique suffix for user details to ensure they are new for each test run
    private final String testSuffix = UUID.randomUUID().toString().substring(0, 8);
    private final String testUsername = "testUser_" + testSuffix;
    private final String testEmail = "test_" + testSuffix + "@example.com";
    private final String testPassword = "password123";
    private final String testVehicleNum = "VEH-" + testSuffix;
    private final String testCarType = "Electric"; // Make sure this is a valid option in your spinner
    private final String testName = "Test";
    private final String testSurname = "User";
    private String testUserUid;


    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setUp() {
        Intents.init();
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        // It's good practice to sign out any existing user before running auth tests
        // to ensure a clean state, though createUserWithEmailAndPassword creates a new user.
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    @After
    public void tearDown() {
        Intents.release();
        // Clean up the created user from Firebase Auth and Firestore
        if (testUserUid != null) {
            final CountDownLatch deleteLatch = new CountDownLatch(2); // For Auth and Firestore user deletion

            // Delete from Firestore
            mDb.collection("users").document(testUserUid).delete()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        System.out.println("Warning: Failed to delete user from Firestore: " + testUserUid);
                    }
                    deleteLatch.countDown();
                });

            // Delete from Auth
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null && currentUser.getUid().equals(testUserUid)) {
                 currentUser.delete().addOnCompleteListener(task -> {
                     if (!task.isSuccessful()) {
                         System.out.println("Warning: Failed to delete user from Auth: " + testUserUid + ", Error: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                     }
                     deleteLatch.countDown();
                 });
            } else {
                System.out.println("Warning: Cannot delete user from Auth. Current user is null or UID does not match.");
                deleteLatch.countDown(); // If not the correct user or no user, count down anyway
            }

            try {
                if (!deleteLatch.await(10, TimeUnit.SECONDS)) {
                    System.out.println("Warning: Timeout during test user cleanup.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Warning: Interrupted during test user cleanup.");
            }
        }
         // Also delete the vehicle if its ID is known and stored
        // For simplicity, this example doesn't store and delete the vehicle separately in cleanup,
        // but in a real scenario, you would.
    }

    @Test
    public void testCreateUserAndLogin_Success() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1); // To wait for user creation

        // 1. Programmatically create user in Firebase Auth and Firestore
        mAuth.createUserWithEmailAndPassword(testEmail, testPassword)
            .addOnCompleteListener(authTask -> {
                if (authTask.isSuccessful()) {
                    FirebaseUser firebaseUser = authTask.getResult().getUser();
                    assertNotNull("Firebase user should not be null after creation", firebaseUser);
                    testUserUid = firebaseUser.getUid();
                    String role = "user";

                    Vehicle vehicle = new Vehicle(testVehicleNum, testCarType);

                    mDb.collection("vehicles").add(vehicle)
                        .addOnSuccessListener(vehicleRef -> {
                            String vehicleId = vehicleRef.getId();
                            User newUser = new User(testName, testSurname, testUsername, testEmail, role, testUserUid, vehicleId);

                            mDb.collection("users").document(testUserUid).set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("User " + testUsername + " created successfully in Auth and Firestore.");
                                    latch.countDown(); // Signal that user creation is complete
                                })
                                .addOnFailureListener(e -> {
                                    fail("Failed to save user to Firestore: " + e.getMessage());
                                    latch.countDown();
                                });
                        })
                        .addOnFailureListener(e -> {
                            fail("Failed to save vehicle to Firestore: " + e.getMessage());
                            latch.countDown();
                        });
                } else {
                    fail("Firebase Auth user creation failed: " + Objects.requireNonNull(authTask.getException()).getMessage());
                    latch.countDown();
                }
            });

        // Wait for the user creation process to complete
        assertTrue("Timeout waiting for user creation", latch.await(30, TimeUnit.SECONDS));
        System.out.println("Proceeding to UI login for user: " + testUsername);


        // 2. Perform Login via UI
        onView(withId(R.id.username))
                .perform(typeText(testUsername), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.login)).perform(click());

        // Add a delay to allow Firebase login operations and activity transition
        // Consider using IdlingResource for more robust synchronization in real apps.
        try {
            Thread.sleep(7000); // Increased delay for combined operations
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Verify that HomePage is started
        intended(hasComponent(HomePage.class.getName()));
        System.out.println("Login test successful for user: " + testUsername);
    }
}
