export interface FaceMePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  initialize(options: {license: string }): Promise<{ version: string }>;

  enroll(options: { imageBase64: string, name: string }): Promise<{ collectionId: string }>;

  search(options: { imageBase64: string }): Promise<{ collectionId: string }>;

  changeCollectionName(options: { collectionId: number, name: string }): Promise<{ value: boolean }>;

  getCollectionName(options: { collectionId: number }): Promise<{ name: string }>;

  deleteFace(options: { faceId: number }): Promise<{ value: boolean }>;
}
