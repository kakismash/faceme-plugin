export interface FaceMePlugin {

  echo(options: { value: string }): Promise<{ value: string }>;

  initialize(options: {license: string }): Promise<{ version: string }>;

  enroll(options: { imageBase64: string,
                    name: string,
                    data?: string }): Promise<{ collectionId: string }>;

  search(options: { imageBase64: string }): Promise<{ collectionId?: string,
                                                      confidence?: number,
                                                      name?: string,
                                                      data?: string}>;

  setCollectionName(options: { collectionId: number,
                               name: string }): Promise<{ value: string }>;

  getCollectionName(options: { collectionId: number }): Promise<{ name: string }>;

  setCollectionData(options: { collectionId: number,
                               data: string }): Promise<{ value: string }>;

  getCollectionData(options: { collectionId: number }): Promise<{ data: string }>;

  deleteCollection(options: { collectionId: number }): Promise<{ value: string }>;

}
