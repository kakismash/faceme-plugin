export interface FaceMePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
