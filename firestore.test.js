const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const REGULAR_UID = "viewer_uid_123";
const ADMIN_UID = "admin_uid_999";
const ADMIN_EMAIL = "1royaldjgamingyt@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validMoviePayload(overrides = {}) {
  const now = new Date(Date.now() - 1000);
  return {
    title: "Devara Rising",
    description: "An intense coastal saga of courage and destiny.",
    language: "Telugu",
    category: "Action",
    genres: ["Action", "Drama"],
    releaseYear: 2025,
    durationMinutes: 148,
    rating: 8.7,
    qualityBadge: "4K UHD",
    posterUrl: "https://example.com/poster.jpg",
    posterKey: "telugu",
    streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    trending: true,
    latest: true,
    featured: true,
    licenseConfirmed: true,
    licenseNote: "Licensed Studio Distribution",
    status: "published",
    createdBy: ADMIN_UID,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Public user without login can query published movies, cannot read drafts or list unfiltered", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("movies").doc("pub_1").set(validMoviePayload({ status: "published" }));
    await ctx.firestore().collection("movies").doc("draft_1").set(validMoviePayload({ status: "draft" }));
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("movies").where("status", "==", "published").get());
  await assertSucceeds(unauthDb.collection("movies").doc("pub_1").get());
  await assertFails(unauthDb.collection("movies").get());
  await assertFails(unauthDb.collection("movies").doc("draft_1").get());
});

test("Public user without login cannot create, update, or delete movies", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("movies").doc("movie_1").set(validMoviePayload());
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("movies").doc("movie_unauth").set(validMoviePayload()));
  await assertFails(
    unauthDb.collection("movies").doc("movie_1").update({ title: "Hacked", updatedAt: new Date(Date.now() - 1000) })
  );
  await assertFails(unauthDb.collection("movies").doc("movie_1").delete());
});

test("Verified bootstrap admin can create, update, unpublish, list all, and delete movies", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("movies").doc("movie_telugu_1").set(validMoviePayload())
  );

  await assertSucceeds(
    adminDb.collection("movies").doc("movie_telugu_1").update({
      status: "draft",
      trending: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(adminDb.collection("movies").get());
  await assertSucceeds(adminDb.collection("movies").doc("movie_telugu_1").delete());
});

test("Email spoofing attack with email_verified=false is rejected", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofDb.collection("movies").doc("movie_spoof_1").set(validMoviePayload({ createdBy: "spoof_uid" }))
  );
});

test("Non-admin signed-in user cannot publish, update, or delete movies", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("movies").doc("movie_1").set(validMoviePayload());
  });

  const regularDb = testEnv.authenticatedContext(REGULAR_UID).firestore();
  await assertFails(
    regularDb.collection("movies").doc("movie_reg").set(validMoviePayload({ createdBy: REGULAR_UID }))
  );
  await assertFails(
    regularDb.collection("movies").doc("movie_1").update({ title: "Hacked", updatedAt: new Date(Date.now() - 1000) })
  );
  await assertFails(regularDb.collection("movies").doc("movie_1").delete());
});

test("Movie creation fails without licenseConfirmed=true or with ghost field", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertFails(
    adminDb.collection("movies").doc("movie_unlicensed").set(validMoviePayload({ licenseConfirmed: false }))
  );
  await assertFails(
    adminDb.collection("movies").doc("movie_ghost").set(validMoviePayload({ isFreeHack: true }))
  );
});

test("Users cannot self-promote in admins collection", async () => {
  const regularDb = testEnv.authenticatedContext(REGULAR_UID).firestore();
  await assertFails(
    regularDb.collection("admins").doc(REGULAR_UID).set({ role: "admin" })
  );
});
