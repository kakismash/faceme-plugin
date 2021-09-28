export interface FaceMePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  initialize(options: {value: string }): Promise<{ value: string }>;

  enrollingFace(options: { imageBase64: string }): Promise<{ value: string }>;

  searchFace(options: { imageBase64: string }): Promise<{ value: string }>;

}
