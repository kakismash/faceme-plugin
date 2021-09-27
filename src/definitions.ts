export interface FaceMePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  initialize(options: {value: string }): Promise<{ value: string }>;

}
