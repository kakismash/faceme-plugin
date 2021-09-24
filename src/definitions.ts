export interface FacemePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
