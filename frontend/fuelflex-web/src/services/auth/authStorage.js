const ACCESS_TOKEN_KEY = "fuelflex_access_token";
const USER_KEY = "fuelflex_user";

function parseStoredValue(value, fallback = null) {
  if (!value) {
    return fallback;
  }

  try {
    return JSON.parse(value);
  } catch {
    return fallback;
  }
}

function getActiveStorage() {
  const localToken = localStorage.getItem(ACCESS_TOKEN_KEY);

  if (localToken) {
    return localStorage;
  }

  const sessionToken = sessionStorage.getItem(ACCESS_TOKEN_KEY);

  if (sessionToken) {
    return sessionStorage;
  }

  return null;
}

export function getAccessToken() {
  const storage = getActiveStorage();

  return storage?.getItem(ACCESS_TOKEN_KEY) || null;
}

export function getStoredUser() {
  const storage = getActiveStorage();

  if (!storage) {
    return null;
  }

  return parseStoredValue(
    storage.getItem(USER_KEY),
    null
  );
}

export function getAuthSession() {
  const storage = getActiveStorage();

  if (!storage) {
    return null;
  }

  const accessToken =
    storage.getItem(ACCESS_TOKEN_KEY);

  if (!accessToken) {
    return null;
  }

  return {
    accessToken,
    user: parseStoredValue(
      storage.getItem(USER_KEY),
      null
    ),
    persistent: storage === localStorage,
  };
}

export function saveAuthSession({
  accessToken,
  user,
  persistent = false,
}) {
  const targetStorage = persistent
    ? localStorage
    : sessionStorage;

  const otherStorage = persistent
    ? sessionStorage
    : localStorage;

  otherStorage.removeItem(ACCESS_TOKEN_KEY);
  otherStorage.removeItem(USER_KEY);

  if (accessToken) {
    targetStorage.setItem(
      ACCESS_TOKEN_KEY,
      accessToken
    );
  }

  if (user) {
    targetStorage.setItem(
      USER_KEY,
      JSON.stringify(user)
    );
  }
}

export function updateStoredUser(updatedUser) {
  const storage = getActiveStorage();

  if (!storage || !updatedUser) {
    return null;
  }

  storage.setItem(
    USER_KEY,
    JSON.stringify(updatedUser)
  );

  return updatedUser;
}

export function mergeStoredUser(userUpdates) {
  const currentUser = getStoredUser() || {};

  const updatedUser = {
    ...currentUser,
    ...userUpdates,
  };

  return updateStoredUser(updatedUser);
}

export function clearAuthSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);

  sessionStorage.removeItem(ACCESS_TOKEN_KEY);
  sessionStorage.removeItem(USER_KEY);
}

export function hasAuthenticatedSession() {
  return Boolean(getAccessToken());
}