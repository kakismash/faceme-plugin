export interface FaceMePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  inizialize(licenseKey: string): Promise<void>;

}
