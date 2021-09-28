export interface FaceMePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  initialize(options: {value: string }): Promise<{ value: string }>;

  register(): Promise<void>;

  //detectBitmap(options: { presentationMs: number,  bitmap: string}): Promise<{ value: string }>;

}
