# faceme

Plugin to connect FaceMe SDK with Ionic Framework

## Install

```bash
npm install faceme
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`initialize(...)`](#initialize)
* [`enroll(...)`](#enroll)
* [`search(...)`](#search)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => any
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>any</code>

--------------------


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
enroll(options: { imageBase64: string; name: string; }) => any
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code>{ imageBase64: string; name: string; }</code> |

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

</docgen-api>
