export interface FaceMePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  initialize(options: {license: string }): Promise<{ version: string }>;

  enrollingFace(options: { imageBase64: string, name: string }): Promise<{ collectionId: string }>;

  searchFace(options: { imageBase64: string }): Promise<{ collectionId: string }>;
}
