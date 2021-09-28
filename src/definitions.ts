export interface FaceMePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  initialize(options: {value: string }): Promise<{ value: string }>;

  enrollingFace(options: { imageBase64: string }): Promise<{ faceId: string }>;

  searchFace(options: { imageBase64: string }): Promise<{ faceId: string }>;

  changeCollectionName(options: { collectionId: number, name: string }): Promise<{ value: string }>;

  getCollectionName(options: { collectionId: number }): Promise<{ name: string }>;
}
