export interface FacemePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  inizialize(licenseKey: string): Promise<string>;
}
