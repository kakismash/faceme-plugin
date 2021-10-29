# faceme

Plugin to connect FaceMe SDK with Ionic Framework

## Install

```bash
npm install faceme
npx cap sync
```

## API

<docgen-index>

* [`initialize(...)`](#initialize)
* [`enroll(...)`](#enroll)
* [`search(...)`](#search)
* [`setCollectionName(...)`](#setcollectionname)
* [`getCollectionName(...)`](#getcollectionname)
* [`setCollectionData(...)`](#setcollectiondata)
* [`getCollectionData(...)`](#getcollectiondata)
* [`deleteCollection(...)`](#deletecollection)
* [`initCamera(...)`](#initcamera)
* [`closeCamera()`](#closecamera)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize(...)

```typescript
initialize(options: { license: string; }) => any
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ license: string; }</code> |

**Returns:** <code>any</code>

--------------------


### enroll(...)

```typescript
enroll(options: { imageBase64: string; name: string; data?: string; }) => any
```

| Param         | Type                                                               |
| ------------- | ------------------------------------------------------------------ |
| **`options`** | <code>{ imageBase64: string; name: string; data?: string; }</code> |

**Returns:** <code>any</code>

--------------------


### search(...)

```typescript
search(options: { imageBase64: string; }) => any
```

| Param         | Type                                  |
| ------------- | ------------------------------------- |
| **`options`** | <code>{ imageBase64: string; }</code> |

**Returns:** <code>any</code>

--------------------


### setCollectionName(...)

```typescript
setCollectionName(options: { collectionId: number; name: string; }) => any
```

| Param         | Type                                                 |
| ------------- | ---------------------------------------------------- |
| **`options`** | <code>{ collectionId: number; name: string; }</code> |

**Returns:** <code>any</code>

--------------------


### getCollectionName(...)

```typescript
getCollectionName(options: { collectionId: number; }) => any
```

| Param         | Type                                   |
| ------------- | -------------------------------------- |
| **`options`** | <code>{ collectionId: number; }</code> |

**Returns:** <code>any</code>

--------------------


### setCollectionData(...)

```typescript
setCollectionData(options: { collectionId: number; data: string; }) => any
```

| Param         | Type                                                 |
| ------------- | ---------------------------------------------------- |
| **`options`** | <code>{ collectionId: number; data: string; }</code> |

**Returns:** <code>any</code>

--------------------


### getCollectionData(...)

```typescript
getCollectionData(options: { collectionId: number; }) => any
```

| Param         | Type                                   |
| ------------- | -------------------------------------- |
| **`options`** | <code>{ collectionId: number; }</code> |

**Returns:** <code>any</code>

--------------------


### deleteCollection(...)

```typescript
deleteCollection(options: { collectionId: number; }) => any
```

| Param         | Type                                   |
| ------------- | -------------------------------------- |
| **`options`** | <code>{ collectionId: number; }</code> |

**Returns:** <code>any</code>

--------------------


### initCamera(...)

```typescript
initCamera(options: { x: number; y: number; width: number; height: number; paddingBottom: number; position: string; }) => any
```

| Param         | Type                                                                                                           |
| ------------- | -------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ x: number; y: number; width: number; height: number; paddingBottom: number; position: string; }</code> |

**Returns:** <code>any</code>

--------------------


### closeCamera()

```typescript
closeCamera() => any
```

**Returns:** <code>any</code>

--------------------

</docgen-api>
