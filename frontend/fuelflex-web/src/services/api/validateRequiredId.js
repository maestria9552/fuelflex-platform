export function validateRequiredId(value, label) {
  if (!value) {
    throw new Error(`${label} est obligatoire.`);
  }

  return value;
}
