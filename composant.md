---
title: Modal
desc: Modal is used to show a dialog or a box when you click a button.
source: https://raw.githubusercontent.com/saadeghi/daisyui/refs/heads/master/packages/daisyui/src/components/modal.css
layout: components
classnames:
  component:
  - class: modal
    desc: Modal
  part:
  - class: modal-box
    desc: The content part
  - class: modal-action
    desc: Actions part (buttons, etc.)
  - class: modal-backdrop
    desc: Label that covers the page when modal is open so we can close the modal by clicking outside
  - class: modal-toggle
    desc: Hidden checkbox that controls the state of modal
  modifier:
  - class: modal-open
    desc: Keeps the modal open (you can add this class using JS)
  placement:
  - class: modal-top
    desc: Moves the modal to top
  - class: modal-middle
    desc: Moves the modal to middle
    default: true
  - class: modal-bottom
    desc: Moves the modal to bottom
  - class: modal-start
    desc: Moves the modal to start horizontally
  - class: modal-end
    desc: Moves the modal to end horizontally
# browserSupport:
  chrome: 37
  firefox: 98
  safari: 15.4
  iossafari: 15.4
---

<script>
  import Component from "$components/Component.svelte"
  import Translate from "$components/Translate.svelte"
</script>


## There are 3 methods to use modals
1. [Using HTML `<dialog>` element](#method-1-html-dialog-element-recommended)  
   It needs JS to open but it has better accessibility and we can close it using `Esc` key
2. [Using checkbox](#method-2-checkbox-legacy)  
   A hidden `<input type="checkbox">` to control the state of modal and `<label>` to check/uncheck the checkbox and open/close the modal
3. [Using `<a>` anchor links](#method-3-using-anchor-links-legacy)  
   A link adds a parameter to the URL and you only see the modal when the URL has that parameter

> :INFO:
>
> Opening a modal adds a [scrollbar-gutter](https://developer.mozilla.org/en-US/docs/Web/CSS/scrollbar-gutter) to the page to avoid layout shift on operating systems that have a fixed scrollbar.
> On recent Chromium based browsers vertical scrollbar presence is detected automatically. On Safari and on mobile devices the scrollbar is displayed as overlay so there will not be gutter. On Firefox you need to detect the presence of vertical scrollbar and set the `scrollbar-gutter: stable` or `scrollbar-gutter: unset` on `:root` element yourself.
> If you don't want to use this feature, [you can exclude `rootscrollgutter`](/docs/config/#exclude).

## Method 1. HTML dialog element `recommended`
HTML dialog element is a native way to create modals. It is accessible and we can close the modal using `Esc` key.  
We can open the modal using JS `ID.showModal()` method and close it using `ID.close()` method.  
The ID must be unique for each modal.

### ~Dialog modal
#### opens on click using ID.showModal() method. can be closed using ID.close() method

<button class="btn" onclick={() => my_modal_1.showModal()}>open modal</button>
<dialog id="my_modal_1" class="modal">
  <div class="modal-box">
    <h3 class="font-bold text-lg">Hello!</h3>
    <p class="py-4">Press ESC key or click the button below to close</p>
    <div class="modal-action">
      <form method="dialog">
        <!-- if there is a button in form, it will close the modal -->
        <button class="btn">Close</button>
      </form>
    </div>
  </div>
</dialog>

```html
<!-- Open the modal using ID.showModal() method -->
<button class="$$btn" onclick="my_modal_1.showModal()">open modal</button>
<dialog id="my_modal_1" class="$$modal">
  <div class="$$modal-box">
    <h3 class="text-lg font-bold">Hello!</h3>
    <p class="py-4">Press ESC key or click the button below to close</p>
    <div class="$$modal-action">
      <form method="dialog">
        <!-- if there is a button in form, it will close the modal -->
        <button class="$$btn">Close</button>
      </form>
    </div>
  </div>
</dialog>
```
```jsx
{/* Open the modal using document.getElementById('ID').showModal() method */}
<button className="$$btn" onClick={()=>document.getElementById('my_modal_1').showModal()}>open modal</button>
<dialog id="my_modal_1" className="$$modal">
  <div className="$$modal-box">
    <h3 className="font-bold text-lg">Hello!</h3>
    <p className="py-4">Press ESC key or click the button below to close</p>
    <div className="$$modal-action">
      <form method="dialog">
        {/* if there is a button in form, it will close the modal */}
        <button className="$$btn">Close</button>
      </form>
    </div>
  </div>
</dialog>
```


### ~Dialog modal, closes when clicked outside
#### there is a second form with 'modal-backdrop' class and it covers the screen so we can close the modal when clicked outside

<button class="btn" onclick={() => my_modal_2.showModal()}>open modal</button>
<dialog id="my_modal_2" class="modal">
  <div class="modal-box">
    <h3 class="font-bold text-lg">Hello!</h3>
    <p class="py-4">Press ESC key or click outside to close</p>
  </div>
  <form method="dialog" class="modal-backdrop">
    <button>close</button>
  </form>
</dialog>

```html
<!-- Open the modal using ID.showModal() method -->
<button class="$$btn" onclick="my_modal_2.showModal()">open modal</button>
<dialog id="my_modal_2" class="$$modal">
  <div class="$$modal-box">
    <h3 class="text-lg font-bold">Hello!</h3>
    <p class="py-4">Press ESC key or click outside to close</p>
  </div>
  <form method="dialog" class="$$modal-backdrop">
    <button>close</button>
  </form>
</dialog>
```
```jsx
{/* Open the modal using document.getElementById('ID').showModal() method */}
<button className="$$btn" onClick={()=>document.getElementById('my_modal_2').showModal()}>open modal</button>
<dialog id="my_modal_2" className="$$modal">
  <div className="$$modal-box">
    <h3 className="font-bold text-lg">Hello!</h3>
    <p className="py-4">Press ESC key or click outside to close</p>
  </div>
  <form method="dialog" className="$$modal-backdrop">
    <button>close</button>
  </form>
</dialog>
```


### ~Dialog modal with a close button at corner
<button class="btn" onclick={() => my_modal_3.showModal()}>open modal</button>
<dialog id="my_modal_3" class="modal">
  <div class="modal-box">
    <form method="dialog">
      <!-- if there is a button in form, it will close the modal -->
      <button class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">✕</button>
    </form>
    <h3 class="font-bold text-lg">Hello!</h3>
    <p class="py-4">Press ESC key or click on ✕ button to close</p>
  </div>
</dialog>

```html
<!-- You can open the modal using ID.showModal() method -->
<button class="$$btn" onclick="my_modal_3.showModal()">open modal</button>
<dialog id="my_modal_3" class="$$modal">
  <div class="$$modal-box">
    <form method="dialog">
      <button class="$$btn $$btn-sm $$btn-circle $$btn-ghost absolute right-2 top-2">✕</button>
    </form>
    <h3 class="text-lg font-bold">Hello!</h3>
    <p class="py-4">Press ESC key or click on ✕ button to close</p>
  </div>
</dialog>
```
```jsx
{/* You can open the modal using document.getElementById('ID').showModal() method */}
<button className="$$btn" onClick={()=>document.getElementById('my_modal_3').showModal()}>open modal</button>
<dialog id="my_modal_3" className="$$modal">
  <div className="$$modal-box">
    <form method="dialog">
      {/* if there is a button in form, it will close the modal */}
      <button className="$$btn $$btn-sm $$btn-circle $$btn-ghost absolute right-2 top-2">✕</button>
    </form>
    <h3 className="font-bold text-lg">Hello!</h3>
    <p className="py-4">Press ESC key or click on ✕ button to close</p>
  </div>
</dialog>
```


### ~Dialog modal with custom width
#### You can use any w-* and max-w-* utility class to customize the width

<button class="btn" onclick={() => my_modal_4.showModal()}>open modal</button>
<dialog id="my_modal_4" class="modal">
  <div class="modal-box w-11/12 max-w-5xl">
    <h3 class="font-bold text-lg">Hello!</h3>
    <p class="py-4">Click the button below to close</p>
    <div class="modal-action">
      <form method="dialog">
        <!-- if there is a button, it will close the modal -->
        <button class="btn">Close</button>
      </form>
    </div>
  </div>
</dialog>

```html
<!-- You can open the modal using ID.showModal() method -->
<button class="$$btn" onclick="my_modal_4.showModal()">open modal</button>
<dialog id="my_modal_4" class="$$modal">
  <div class="$$modal-box w-11/12 max-w-5xl">
    <h3 class="text-lg font-bold">Hello!</h3>
    <p class="py-4">Click the button below to close</p>
    <div class="$$modal-action">
      <form method="dialog">
        <!-- if there is a button, it will close the modal -->
        <button class="$$btn">Close</button>
      </form>
    </div>
  </div>
</dialog>
```
```jsx
{/* You can open the modal using document.getElementById('ID').showModal() method */}
<button className="$$btn" onClick={()=>document.getElementById('my_modal_4').showModal()}>open modal</button>
<dialog id="my_modal_4" className="$$modal">
  <div className="$$modal-box w-11/12 max-w-5xl">
    <h3 className="font-bold text-lg">Hello!</h3>
    <p className="py-4">Click the button below to close</p>
    <div className="$$modal-action">
      <form method="dialog">
        {/* if there is a button, it will close the modal */}
        <button className="$$btn">Close</button>
      </form>
    </div>
  </div>
</dialog>
```


### ~Responsive
#### Modal goes bottom on SM screen size, goes middle on MD screen size

<button class="btn" onclick={() => my_modal_5.showModal()}>open modal</button>
<dialog id="my_modal_5" class="modal modal-bottom sm:modal-middle">
  <div class="modal-box">
    <h3 class="font-bold text-lg">Hello!</h3>
    <p class="py-4">Press ESC key or click the button below to close</p>
    <div class="modal-action">
      <form method="dialog">
        <!-- if there is a button in form, it will close the modal -->
        <button class="btn">Close</button>
      </form>
    </div>
  </div>
</dialog>

```html
<!-- Open the modal using ID.showModal() method -->
<button class="$$btn" onclick="my_modal_5.showModal()">open modal</button>
<dialog id="my_modal_5" class="$$modal $$modal-bottom sm:$$modal-middle">
  <div class="$$modal-box">
    <h3 class="text-lg font-bold">Hello!</h3>
    <p class="py-4">Press ESC key or click the button below to close</p>
    <div class="$$modal-action">
      <form method="dialog">
        <!-- if there is a button in form, it will close the modal -->
        <button class="$$btn">Close</button>
      </form>
    </div>
  </div>
</dialog>
```
```jsx
{/* Open the modal using document.getElementById('ID').showModal() method */}
<button className="$$btn" onClick={()=>document.getElementById('my_modal_5').showModal()}>open modal</button>
<dialog id="my_modal_5" className="$$modal $$modal-bottom sm:$$modal-middle">
  <div className="$$modal-box">
    <h3 className="font-bold text-lg">Hello!</h3>
    <p className="py-4">Press ESC key or click the button below to close</p>
    <div className="$$modal-action">
      <form method="dialog">
        {/* if there is a button in form, it will close the modal */}
        <button className="$$btn">Close</button>
      </form>
    </div>
  </div>
</dialog>
```


## Method 2. checkbox `legacy`
A hidden checkbox can control the state of modal and labels can toggle the checkbox so we can open/close the modal.


### ~Modal using checkbox
<label for="my_modal_6" class="btn">open modal</label>

```html
<!-- The button to open modal -->
<label for="my_modal_6" class="$$btn">open modal</label>

<!-- Put this part before </body> tag -->
<input type="checkbox" id="my_modal_6" class="$$modal-toggle" />
<div class="$$modal" role="dialog">
  <div class="$$modal-box">
    <h3 class="text-lg font-bold">Hello!</h3>
    <p class="py-4">This modal works with a hidden checkbox!</p>
    <div class="$$modal-action">
      <label for="my_modal_6" class="$$btn">Close!</label>
    </div>
  </div>
</div>
```


### ~Modal that closes when clicked outside
#### Modal works with a hidden checkbox and labels can toggle the checkbox so we can use another label tag with 'modal-backdrop' class that covers the screen so we can close the modal when clicked outside

<label for="my_modal_7" class="btn">open modal</label>

```html
<!-- The button to open modal -->
<label for="my_modal_7" class="$$btn">open modal</label>

<!-- Put this part before </body> tag -->
<input type="checkbox" id="my_modal_7" class="$$modal-toggle" />
<div class="$$modal" role="dialog">
  <div class="$$modal-box">
    <h3 class="text-lg font-bold">Hello!</h3>
    <p class="py-4">This modal works with a hidden checkbox!</p>
  </div>
  <label class="$$modal-backdrop" for="my_modal_7">Close</label>
</div>
```


### Method 3. using anchor links `legacy`
A link adds a parameter to the URL and you only see the modal when the URL has that parameter  
When modal is closed, the page will scroll to the top because of the anchor link.
Anchor links might not work well on some SPA frameworks. If there are problems, use the other methods

### ~Modal using anchor link

<a href="#my_modal_8" class="btn" rel="external">open modal</a>

```html
<!-- The button to open modal -->
<a href="#my_modal_8" class="$$btn">open modal</a>

<!-- Put this part before </body> tag -->
<div class="$$modal" role="dialog" id="my_modal_8">
  <div class="$$modal-box">
    <h3 class="text-lg font-bold">Hello!</h3>
    <p class="py-4">This modal works with anchor links</p>
    <div class="$$modal-action">
      <a href="#" class="$$btn">Yay!</a>
    </div>
  </div>
</div>
```
# Tableau

---
title: Table
desc: Table can be used to show a list of data in a table format.
source: https://raw.githubusercontent.com/saadeghi/daisyui/refs/heads/master/packages/daisyui/src/components/table.css
layout: components
classnames:
component:
- class: table
  desc: For <table> tag
  modifier:
- class: table-zebra
  desc: For <table> to show zebra stripe rows
- class: table-pin-rows
  desc: For <table> to make all the rows inside <thead> and <tfoot> sticky
- class: table-pin-cols
  desc: For <table> to make all the <th> columns sticky
  size:
- class: table-xs
  desc: Extra small size
- class: table-sm
  desc: Small size
- class: table-md
  desc: Medium size
  default: true
- class: table-lg
  desc: Large size
- class: table-xl
  desc: Extra large size
---

<script>
  import Component from "$components/Component.svelte"
  import Translate from "$components/Translate.svelte"
</script>

### ~Table
<div class="overflow-x-auto">
  <table class="table">
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>

```html
<div class="overflow-x-auto">
  <table class="$$table">
    <!-- head -->
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <!-- row 1 -->
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <!-- row 2 -->
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <!-- row 3 -->
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>
```

### ~Table with border and background
<div class="overflow-x-auto rounded-box border border-base-content/5 bg-base-100">
  <table class="table">
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>

```html
<div class="overflow-x-auto rounded-box border border-base-content/5 bg-base-100">
  <table class="$$table">
    <!-- head -->
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <!-- row 1 -->
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <!-- row 2 -->
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <!-- row 3 -->
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>
```

### ~Table with an active row
<div class="overflow-x-auto">
  <table class="table">
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <tr class="bg-base-200">
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>

```html
<div class="overflow-x-auto">
  <table class="$$table">
    <!-- head -->
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <!-- row 1 -->
      <tr class="bg-base-200">
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <!-- row 2 -->
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <!-- row 3 -->
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>
```


### ~Table with a row that highlights on hover
<div class="overflow-x-auto">
  <table class="table">
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <tr class="hover:bg-base-300">
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>

```html
<div class="overflow-x-auto">
  <table class="$$table">
    <!-- head -->
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <!-- row 1 -->
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <!-- row 2 -->
      <tr class="hover:bg-base-300">
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <!-- row 3 -->
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>
```


### ~Zebra
<div class="overflow-x-auto">
  <table class="table table-zebra">
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>

```html
<div class="overflow-x-auto">
  <table class="$$table $$table-zebra">
    <!-- head -->
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <!-- row 1 -->
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Blue</td>
      </tr>
      <!-- row 2 -->
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Purple</td>
      </tr>
      <!-- row 3 -->
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Red</td>
      </tr>
    </tbody>
  </table>
</div>
```


### ~Table with visual elements
<div class="overflow-x-auto">
  <table class="table">
    <thead>
      <tr>
        <th>
          <label>
            <input type="checkbox" class="checkbox" />
          </label>
        </th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th>
          <label>
            <input type="checkbox" class="checkbox" />
          </label>
        </th>
        <td>
          <div class="flex items-center gap-3">
            <div class="avatar">
              <div class="w-12 h-12 mask mask-squircle">
                <img src="https://img.daisyui.com/images/profile/demo/2@94.webp" alt="Avatar Tailwind CSS Component" />
              </div>
            </div>
            <div>
              <div class="font-bold">Hart Hagerty</div>
              <div class="text-sm opacity-50">United States</div>
            </div>
          </div>
        </td>
        <td>
          Zemlak, Daniel and Leannon
          <br/>
          <span class="badge badge-ghost badge-sm">Desktop Support Technician</span>
        </td>
        <td>Purple</td>
        <th>
          <button class="btn btn-ghost btn-xs">details</button>
        </th>
      </tr>
      <tr>
        <th>
          <label>
            <input type="checkbox" class="checkbox" />
          </label>
        </th>
        <td>
          <div class="flex items-center gap-3">
            <div class="avatar">
              <div class="w-12 h-12 mask mask-squircle">
                <img src="https://img.daisyui.com/images/profile/demo/3@94.webp" alt="Avatar Tailwind CSS Component" />
              </div>
            </div>
            <div>
              <div class="font-bold">Brice Swyre</div>
              <div class="text-sm opacity-50">China</div>
            </div>
          </div>
        </td>
        <td>
          Carroll Group
          <br/>
          <span class="badge badge-ghost badge-sm">Tax Accountant</span>
        </td>
        <td>Red</td>
        <th>
          <button class="btn btn-ghost btn-xs">details</button>
        </th>
      </tr>
      <tr>
        <th>
          <label>
            <input type="checkbox" class="checkbox" />
          </label>
        </th>
        <td>
          <div class="flex items-center gap-3">
            <div class="avatar">
              <div class="w-12 h-12 mask mask-squircle">
                <img src="https://img.daisyui.com/images/profile/demo/4@94.webp" alt="Avatar Tailwind CSS Component" />
              </div>
            </div>
            <div>
              <div class="font-bold">Marjy Ferencz</div>
              <div class="text-sm opacity-50">Russia</div>
            </div>
          </div>
        </td>
        <td>
          Rowe-Schoen
          <br/>
          <span class="badge badge-ghost badge-sm">Office Assistant I</span>
        </td>
        <td>Crimson</td>
        <th>
          <button class="btn btn-ghost btn-xs">details</button>
        </th>
      </tr>
      <tr>
        <th>
          <label>
            <input type="checkbox" class="checkbox" />
          </label>
        </th>
        <td>
          <div class="flex items-center gap-3">
            <div class="avatar">
              <div class="w-12 h-12 mask mask-squircle">
                <img src="https://img.daisyui.com/images/profile/demo/5@94.webp" alt="Avatar Tailwind CSS Component" />
              </div>
            </div>
            <div>
              <div class="font-bold">Yancy Tear</div>
              <div class="text-sm opacity-50">Brazil</div>
            </div>
          </div>
        </td>
        <td>
          Wyman-Ledner
          <br/>
          <span class="badge badge-ghost badge-sm">Community Outreach Specialist</span>
        </td>
        <td>Indigo</td>
        <th>
          <button class="btn btn-ghost btn-xs">details</button>
        </th>
      </tr>
    </tbody>
    <tfoot>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
        <th></th>
      </tr>
    </tfoot>
  </table>
</div>

```html
<div class="overflow-x-auto">
  <table class="$$table">
    <!-- head -->
    <thead>
      <tr>
        <th>
          <label>
            <input type="checkbox" class="$$checkbox" />
          </label>
        </th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <!-- row 1 -->
      <tr>
        <th>
          <label>
            <input type="checkbox" class="$$checkbox" />
          </label>
        </th>
        <td>
          <div class="flex items-center gap-3">
            <div class="$$avatar">
              <div class="$$mask $$mask-squircle h-12 w-12">
                <img
                  src="https://img.daisyui.com/images/profile/demo/2@94.webp"
                  alt="Avatar Tailwind CSS Component" />
              </div>
            </div>
            <div>
              <div class="font-bold">Hart Hagerty</div>
              <div class="text-sm opacity-50">United States</div>
            </div>
          </div>
        </td>
        <td>
          Zemlak, Daniel and Leannon
          <br />
          <span class="$$badge $$badge-ghost $$badge-sm">Desktop Support Technician</span>
        </td>
        <td>Purple</td>
        <th>
          <button class="$$btn $$btn-ghost $$btn-xs">details</button>
        </th>
      </tr>
      <!-- row 2 -->
      <tr>
        <th>
          <label>
            <input type="checkbox" class="$$checkbox" />
          </label>
        </th>
        <td>
          <div class="flex items-center gap-3">
            <div class="$$avatar">
              <div class="$$mask $$mask-squircle h-12 w-12">
                <img
                  src="https://img.daisyui.com/images/profile/demo/3@94.webp"
                  alt="Avatar Tailwind CSS Component" />
              </div>
            </div>
            <div>
              <div class="font-bold">Brice Swyre</div>
              <div class="text-sm opacity-50">China</div>
            </div>
          </div>
        </td>
        <td>
          Carroll Group
          <br />
          <span class="$$badge $$badge-ghost $$badge-sm">Tax Accountant</span>
        </td>
        <td>Red</td>
        <th>
          <button class="$$btn $$btn-ghost $$btn-xs">details</button>
        </th>
      </tr>
      <!-- row 3 -->
      <tr>
        <th>
          <label>
            <input type="checkbox" class="$$checkbox" />
          </label>
        </th>
        <td>
          <div class="flex items-center gap-3">
            <div class="$$avatar">
              <div class="$$mask $$mask-squircle h-12 w-12">
                <img
                  src="https://img.daisyui.com/images/profile/demo/4@94.webp"
                  alt="Avatar Tailwind CSS Component" />
              </div>
            </div>
            <div>
              <div class="font-bold">Marjy Ferencz</div>
              <div class="text-sm opacity-50">Russia</div>
            </div>
          </div>
        </td>
        <td>
          Rowe-Schoen
          <br />
          <span class="$$badge $$badge-ghost $$badge-sm">Office Assistant I</span>
        </td>
        <td>Crimson</td>
        <th>
          <button class="$$btn $$btn-ghost $$btn-xs">details</button>
        </th>
      </tr>
      <!-- row 4 -->
      <tr>
        <th>
          <label>
            <input type="checkbox" class="$$checkbox" />
          </label>
        </th>
        <td>
          <div class="flex items-center gap-3">
            <div class="$$avatar">
              <div class="$$mask $$mask-squircle h-12 w-12">
                <img
                  src="https://img.daisyui.com/images/profile/demo/5@94.webp"
                  alt="Avatar Tailwind CSS Component" />
              </div>
            </div>
            <div>
              <div class="font-bold">Yancy Tear</div>
              <div class="text-sm opacity-50">Brazil</div>
            </div>
          </div>
        </td>
        <td>
          Wyman-Ledner
          <br />
          <span class="$$badge $$badge-ghost $$badge-sm">Community Outreach Specialist</span>
        </td>
        <td>Indigo</td>
        <th>
          <button class="$$btn $$btn-ghost $$btn-xs">details</button>
        </th>
      </tr>
    </tbody>
    <!-- foot -->
    <tfoot>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>Favorite Color</th>
        <th></th>
      </tr>
    </tfoot>
  </table>
</div>
```


### ~Table xs
<div class="overflow-x-auto">
  <table class="table table-xs">
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>company</th>
        <th>location</th>
        <th>Last Login</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Littel, Schaden and Vandervort</td>
        <td>Canada</td>
        <td>12/16/2020</td>
        <td>Blue</td>
      </tr>
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Zemlak, Daniel and Leannon</td>
        <td>United States</td>
        <td>12/5/2020</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Carroll Group</td>
        <td>China</td>
        <td>8/15/2020</td>
        <td>Red</td>
      </tr>
      <tr>
        <th>4</th>
        <td>Marjy Ferencz</td>
        <td>Office Assistant I</td>
        <td>Rowe-Schoen</td>
        <td>Russia</td>
        <td>3/25/2021</td>
        <td>Crimson</td>
      </tr>
      <tr>
        <th>5</th>
        <td>Yancy Tear</td>
        <td>Community Outreach Specialist</td>
        <td>Wyman-Ledner</td>
        <td>Brazil</td>
        <td>5/22/2020</td>
        <td>Indigo</td>
      </tr>
      <tr>
        <th>6</th>
        <td>Irma Vasilik</td>
        <td>Editor</td>
        <td>Wiza, Bins and Emard</td>
        <td>Venezuela</td>
        <td>12/8/2020</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>7</th>
        <td>Meghann Durtnal</td>
        <td>Staff Accountant IV</td>
        <td>Schuster-Schimmel</td>
        <td>Philippines</td>
        <td>2/17/2021</td>
        <td>Yellow</td>
      </tr>
      <tr>
        <th>8</th>
        <td>Sammy Seston</td>
        <td>Accountant I</td>
        <td>O'Hara, Welch and Keebler</td>
        <td>Indonesia</td>
        <td>5/23/2020</td>
        <td>Crimson</td>
      </tr>
      <tr>
        <th>9</th>
        <td>Lesya Tinham</td>
        <td>Safety Technician IV</td>
        <td>Turner-Kuhlman</td>
        <td>Philippines</td>
        <td>2/21/2021</td>
        <td>Maroon</td>
      </tr>
      <tr>
        <th>10</th>
        <td>Zaneta Tewkesbury</td>
        <td>VP Marketing</td>
        <td>Sauer LLC</td>
        <td>Chad</td>
        <td>6/23/2020</td>
        <td>Green</td>
      </tr>
      <tr>
        <th>11</th>
        <td>Andy Tipple</td>
        <td>Librarian</td>
        <td>Hilpert Group</td>
        <td>Poland</td>
        <td>7/9/2020</td>
        <td>Indigo</td>
      </tr>
      <tr>
        <th>12</th>
        <td>Sophi Biles</td>
        <td>Recruiting Manager</td>
        <td>Gutmann Inc</td>
        <td>Indonesia</td>
        <td>2/12/2021</td>
        <td>Maroon</td>
      </tr>
      <tr>
        <th>13</th>
        <td>Florida Garces</td>
        <td>Web Developer IV</td>
        <td>Gaylord, Pacocha and Baumbach</td>
        <td>Poland</td>
        <td>5/31/2020</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>14</th>
        <td>Maribeth Popping</td>
        <td>Analyst Programmer</td>
        <td>Deckow-Pouros</td>
        <td>Portugal</td>
        <td>4/27/2021</td>
        <td>Aquamarine</td>
      </tr>
      <tr>
        <th>15</th>
        <td>Moritz Dryburgh</td>
        <td>Dental Hygienist</td>
        <td>Schiller, Cole and Hackett</td>
        <td>Sri Lanka</td>
        <td>8/8/2020</td>
        <td>Crimson</td>
      </tr>
      <tr>
        <th>16</th>
        <td>Reid Semiras</td>
        <td>Teacher</td>
        <td>Sporer, Sipes and Rogahn</td>
        <td>Poland</td>
        <td>7/30/2020</td>
        <td>Green</td>
      </tr>
      <tr>
        <th>17</th>
        <td>Alec Lethby</td>
        <td>Teacher</td>
        <td>Reichel, Glover and Hamill</td>
        <td>China</td>
        <td>2/28/2021</td>
        <td>Khaki</td>
      </tr>
      <tr>
        <th>18</th>
        <td>Aland Wilber</td>
        <td>Quality Control Specialist</td>
        <td>Kshlerin, Rogahn and Swaniawski</td>
        <td>Czech Republic</td>
        <td>9/29/2020</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>19</th>
        <td>Teddie Duerden</td>
        <td>Staff Accountant III</td>
        <td>Pouros, Ullrich and Windler</td>
        <td>France</td>
        <td>10/27/2020</td>
        <td>Aquamarine</td>
      </tr>
      <tr>
        <th>20</th>
        <td>Lorelei Blackstone</td>
        <td>Data Coordinator</td>
        <td>Witting, Kutch and Greenfelder</td>
        <td>Kazakhstan</td>
        <td>6/3/2020</td>
        <td>Red</td>
      </tr>
    </tbody>
    <tfoot>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>company</th>
        <th>location</th>
        <th>Last Login</th>
        <th>Favorite Color</th>
      </tr>
    </tfoot>
  </table>
</div>

```html
<div class="overflow-x-auto">
  <table class="$$table $$table-xs">
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>company</th>
        <th>location</th>
        <th>Last Login</th>
        <th>Favorite Color</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Littel, Schaden and Vandervort</td>
        <td>Canada</td>
        <td>12/16/2020</td>
        <td>Blue</td>
      </tr>
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Zemlak, Daniel and Leannon</td>
        <td>United States</td>
        <td>12/5/2020</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Carroll Group</td>
        <td>China</td>
        <td>8/15/2020</td>
        <td>Red</td>
      </tr>
      <tr>
        <th>4</th>
        <td>Marjy Ferencz</td>
        <td>Office Assistant I</td>
        <td>Rowe-Schoen</td>
        <td>Russia</td>
        <td>3/25/2021</td>
        <td>Crimson</td>
      </tr>
      <tr>
        <th>5</th>
        <td>Yancy Tear</td>
        <td>Community Outreach Specialist</td>
        <td>Wyman-Ledner</td>
        <td>Brazil</td>
        <td>5/22/2020</td>
        <td>Indigo</td>
      </tr>
      <tr>
        <th>6</th>
        <td>Irma Vasilik</td>
        <td>Editor</td>
        <td>Wiza, Bins and Emard</td>
        <td>Venezuela</td>
        <td>12/8/2020</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>7</th>
        <td>Meghann Durtnal</td>
        <td>Staff Accountant IV</td>
        <td>Schuster-Schimmel</td>
        <td>Philippines</td>
        <td>2/17/2021</td>
        <td>Yellow</td>
      </tr>
      <tr>
        <th>8</th>
        <td>Sammy Seston</td>
        <td>Accountant I</td>
        <td>O'Hara, Welch and Keebler</td>
        <td>Indonesia</td>
        <td>5/23/2020</td>
        <td>Crimson</td>
      </tr>
      <tr>
        <th>9</th>
        <td>Lesya Tinham</td>
        <td>Safety Technician IV</td>
        <td>Turner-Kuhlman</td>
        <td>Philippines</td>
        <td>2/21/2021</td>
        <td>Maroon</td>
      </tr>
      <tr>
        <th>10</th>
        <td>Zaneta Tewkesbury</td>
        <td>VP Marketing</td>
        <td>Sauer LLC</td>
        <td>Chad</td>
        <td>6/23/2020</td>
        <td>Green</td>
      </tr>
      <tr>
        <th>11</th>
        <td>Andy Tipple</td>
        <td>Librarian</td>
        <td>Hilpert Group</td>
        <td>Poland</td>
        <td>7/9/2020</td>
        <td>Indigo</td>
      </tr>
      <tr>
        <th>12</th>
        <td>Sophi Biles</td>
        <td>Recruiting Manager</td>
        <td>Gutmann Inc</td>
        <td>Indonesia</td>
        <td>2/12/2021</td>
        <td>Maroon</td>
      </tr>
      <tr>
        <th>13</th>
        <td>Florida Garces</td>
        <td>Web Developer IV</td>
        <td>Gaylord, Pacocha and Baumbach</td>
        <td>Poland</td>
        <td>5/31/2020</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>14</th>
        <td>Maribeth Popping</td>
        <td>Analyst Programmer</td>
        <td>Deckow-Pouros</td>
        <td>Portugal</td>
        <td>4/27/2021</td>
        <td>Aquamarine</td>
      </tr>
      <tr>
        <th>15</th>
        <td>Moritz Dryburgh</td>
        <td>Dental Hygienist</td>
        <td>Schiller, Cole and Hackett</td>
        <td>Sri Lanka</td>
        <td>8/8/2020</td>
        <td>Crimson</td>
      </tr>
      <tr>
        <th>16</th>
        <td>Reid Semiras</td>
        <td>Teacher</td>
        <td>Sporer, Sipes and Rogahn</td>
        <td>Poland</td>
        <td>7/30/2020</td>
        <td>Green</td>
      </tr>
      <tr>
        <th>17</th>
        <td>Alec Lethby</td>
        <td>Teacher</td>
        <td>Reichel, Glover and Hamill</td>
        <td>China</td>
        <td>2/28/2021</td>
        <td>Khaki</td>
      </tr>
      <tr>
        <th>18</th>
        <td>Aland Wilber</td>
        <td>Quality Control Specialist</td>
        <td>Kshlerin, Rogahn and Swaniawski</td>
        <td>Czech Republic</td>
        <td>9/29/2020</td>
        <td>Purple</td>
      </tr>
      <tr>
        <th>19</th>
        <td>Teddie Duerden</td>
        <td>Staff Accountant III</td>
        <td>Pouros, Ullrich and Windler</td>
        <td>France</td>
        <td>10/27/2020</td>
        <td>Aquamarine</td>
      </tr>
      <tr>
        <th>20</th>
        <td>Lorelei Blackstone</td>
        <td>Data Coordiator</td>
        <td>Witting, Kutch and Greenfelder</td>
        <td>Kazakhstan</td>
        <td>6/3/2020</td>
        <td>Red</td>
      </tr>
    </tbody>
    <tfoot>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Job</th>
        <th>company</th>
        <th>location</th>
        <th>Last Login</th>
        <th>Favorite Color</th>
      </tr>
    </tfoot>
  </table>
</div>
```


### ~Table with pinned-rows
<div class="overflow-x-auto h-96">
  <table class="table table-pin-rows bg-base-200">
  <thead>
    <tr>
      <th>A</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Ant-Man</td></tr>
    <tr><td>Aquaman</td></tr>
    <tr><td>Asterix</td></tr>
    <tr><td>The Atom</td></tr>
    <tr><td>The Avengers</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>B</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Batgirl</td></tr>
    <tr><td>Batman</td></tr>
    <tr><td>Batwoman</td></tr>
    <tr><td>Black Canary</td></tr>
    <tr><td>Black Panther</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>C</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Captain America</td></tr>
    <tr><td>Captain Marvel</td></tr>
    <tr><td>Catwoman</td></tr>
    <tr><td>Conan the Barbarian</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>D</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Daredevil</td></tr>
    <tr><td>The Defenders</td></tr>
    <tr><td>Doc Savage</td></tr>
    <tr><td>Doctor Strange</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>E</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Elektra</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>F</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Fantastic Four</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>G</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Ghost Rider</td></tr>
    <tr><td>Green Arrow</td></tr>
    <tr><td>Green Lantern</td></tr>
    <tr><td>Guardians of the Galaxy</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>H</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Hawkeye</td></tr>
    <tr><td>Hellboy</td></tr>
    <tr><td>Incredible Hulk</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>I</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Iron Fist</td></tr>
    <tr><td>Iron Man</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>M</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Marvelman</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>R</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Robin</td></tr>
    <tr><td>The Rocketeer</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>S</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>The Shadow</td></tr>
    <tr><td>Spider-Man</td></tr>
    <tr><td>Sub-Mariner</td></tr>
    <tr><td>Supergirl</td></tr>
    <tr><td>Superman</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>T</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Teenage Mutant Ninja Turtles</td></tr>
    <tr><td>Thor</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>W</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>The Wasp</td></tr>
    <tr><td>Watchmen</td></tr>
    <tr><td>Wolverine</td></tr>
    <tr><td>Wonder Woman</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>X</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>X-Men</td></tr>
  </tbody>
  <thead>
    <tr>
      <th>Z</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Zatanna</td></tr>
    <tr><td>Zatara</td></tr>
  </tbody>
  </table>
</div>

```html
<div class="h-96 overflow-x-auto">
  <table class="$$table $$table-pin-rows bg-base-200">
    <thead>
      <tr>
        <th>A</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Ant-Man</td></tr>
      <tr><td>Aquaman</td></tr>
      <tr><td>Asterix</td></tr>
      <tr><td>The Atom</td></tr>
      <tr><td>The Avengers</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>B</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Batgirl</td></tr>
      <tr><td>Batman</td></tr>
      <tr><td>Batwoman</td></tr>
      <tr><td>Black Canary</td></tr>
      <tr><td>Black Panther</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>C</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Captain America</td></tr>
      <tr><td>Captain Marvel</td></tr>
      <tr><td>Catwoman</td></tr>
      <tr><td>Conan the Barbarian</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>D</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Daredevil</td></tr>
      <tr><td>The Defenders</td></tr>
      <tr><td>Doc Savage</td></tr>
      <tr><td>Doctor Strange</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>E</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Elektra</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>F</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Fantastic Four</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>G</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Ghost Rider</td></tr>
      <tr><td>Green Arrow</td></tr>
      <tr><td>Green Lantern</td></tr>
      <tr><td>Guardians of the Galaxy</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>H</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Hawkeye</td></tr>
      <tr><td>Hellboy</td></tr>
      <tr><td>Incredible Hulk</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>I</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Iron Fist</td></tr>
      <tr><td>Iron Man</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>M</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Marvelman</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>R</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Robin</td></tr>
      <tr><td>The Rocketeer</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>S</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>The Shadow</td></tr>
      <tr><td>Spider-Man</td></tr>
      <tr><td>Sub-Mariner</td></tr>
      <tr><td>Supergirl</td></tr>
      <tr><td>Superman</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>T</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Teenage Mutant Ninja Turtles</td></tr>
      <tr><td>Thor</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>W</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>The Wasp</td></tr>
      <tr><td>Watchmen</td></tr>
      <tr><td>Wolverine</td></tr>
      <tr><td>Wonder Woman</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>X</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>X-Men</td></tr>
    </tbody>
    <thead>
      <tr>
        <th>Z</th>
      </tr>
    </thead>
    <tbody>
      <tr><td>Zatanna</td></tr>
      <tr><td>Zatara</td></tr>
    </tbody>
  </table>
</div>
```


### ~Table with pinned-rows and pinned-cols
<div class="overflow-x-auto h-96 w-96">
  <table class="table table-xs table-pin-rows table-pin-cols">
    <thead>
      <tr>
        <th></th>
        <td>Name</td>
        <td>Job</td>
        <td>company</td>
        <td>location</td>
        <td>Last Login</td>
        <td>Favorite Color</td>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Littel, Schaden and Vandervort</td>
        <td>Canada</td>
        <td>12/16/2020</td>
        <td>Blue</td>
        <th>1</th>
      </tr>
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Zemlak, Daniel and Leannon</td>
        <td>United States</td>
        <td>12/5/2020</td>
        <td>Purple</td>
        <th>2</th>
      </tr>
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Carroll Group</td>
        <td>China</td>
        <td>8/15/2020</td>
        <td>Red</td>
        <th>3</th>
      </tr>
      <tr>
        <th>4</th>
        <td>Marjy Ferencz</td>
        <td>Office Assistant I</td>
        <td>Rowe-Schoen</td>
        <td>Russia</td>
        <td>3/25/2021</td>
        <td>Crimson</td>
        <th>4</th>
      </tr>
      <tr>
        <th>5</th>
        <td>Yancy Tear</td>
        <td>Community Outreach Specialist</td>
        <td>Wyman-Ledner</td>
        <td>Brazil</td>
        <td>5/22/2020</td>
        <td>Indigo</td>
        <th>5</th>
      </tr>
      <tr>
        <th>6</th>
        <td>Irma Vasilik</td>
        <td>Editor</td>
        <td>Wiza, Bins and Emard</td>
        <td>Venezuela</td>
        <td>12/8/2020</td>
        <td>Purple</td>
        <th>6</th>
      </tr>
      <tr>
        <th>7</th>
        <td>Meghann Durtnal</td>
        <td>Staff Accountant IV</td>
        <td>Schuster-Schimmel</td>
        <td>Philippines</td>
        <td>2/17/2021</td>
        <td>Yellow</td>
        <th>7</th>
      </tr>
      <tr>
        <th>8</th>
        <td>Sammy Seston</td>
        <td>Accountant I</td>
        <td>O'Hara, Welch and Keebler</td>
        <td>Indonesia</td>
        <td>5/23/2020</td>
        <td>Crimson</td>
        <th>8</th>
      </tr>
      <tr>
        <th>9</th>
        <td>Lesya Tinham</td>
        <td>Safety Technician IV</td>
        <td>Turner-Kuhlman</td>
        <td>Philippines</td>
        <td>2/21/2021</td>
        <td>Maroon</td>
        <th>9</th>
      </tr>
      <tr>
        <th>10</th>
        <td>Zaneta Tewkesbury</td>
        <td>VP Marketing</td>
        <td>Sauer LLC</td>
        <td>Chad</td>
        <td>6/23/2020</td>
        <td>Green</td>
        <th>10</th>
      </tr>
      <tr>
        <th>11</th>
        <td>Andy Tipple</td>
        <td>Librarian</td>
        <td>Hilpert Group</td>
        <td>Poland</td>
        <td>7/9/2020</td>
        <td>Indigo</td>
        <th>11</th>
      </tr>
      <tr>
        <th>12</th>
        <td>Sophi Biles</td>
        <td>Recruiting Manager</td>
        <td>Gutmann Inc</td>
        <td>Indonesia</td>
        <td>2/12/2021</td>
        <td>Maroon</td>
        <th>12</th>
      </tr>
      <tr>
        <th>13</th>
        <td>Florida Garces</td>
        <td>Web Developer IV</td>
        <td>Gaylord, Pacocha and Baumbach</td>
        <td>Poland</td>
        <td>5/31/2020</td>
        <td>Purple</td>
        <th>13</th>
      </tr>
      <tr>
        <th>14</th>
        <td>Maribeth Popping</td>
        <td>Analyst Programmer</td>
        <td>Deckow-Pouros</td>
        <td>Portugal</td>
        <td>4/27/2021</td>
        <td>Aquamarine</td>
        <th>14</th>
      </tr>
      <tr>
        <th>15</th>
        <td>Moritz Dryburgh</td>
        <td>Dental Hygienist</td>
        <td>Schiller, Cole and Hackett</td>
        <td>Sri Lanka</td>
        <td>8/8/2020</td>
        <td>Crimson</td>
        <th>15</th>
      </tr>
      <tr>
        <th>16</th>
        <td>Reid Semiras</td>
        <td>Teacher</td>
        <td>Sporer, Sipes and Rogahn</td>
        <td>Poland</td>
        <td>7/30/2020</td>
        <td>Green</td>
        <th>16</th>
      </tr>
      <tr>
        <th>17</th>
        <td>Alec Lethby</td>
        <td>Teacher</td>
        <td>Reichel, Glover and Hamill</td>
        <td>China</td>
        <td>2/28/2021</td>
        <td>Khaki</td>
        <th>17</th>
      </tr>
      <tr>
        <th>18</th>
        <td>Aland Wilber</td>
        <td>Quality Control Specialist</td>
        <td>Kshlerin, Rogahn and Swaniawski</td>
        <td>Czech Republic</td>
        <td>9/29/2020</td>
        <td>Purple</td>
        <th>18</th>
      </tr>
      <tr>
        <th>19</th>
        <td>Teddie Duerden</td>
        <td>Staff Accountant III</td>
        <td>Pouros, Ullrich and Windler</td>
        <td>France</td>
        <td>10/27/2020</td>
        <td>Aquamarine</td>
        <th>19</th>
      </tr>
      <tr>
        <th>20</th>
        <td>Lorelei Blackstone</td>
        <td>Data Coordinator</td>
        <td>Witting, Kutch and Greenfelder</td>
        <td>Kazakhstan</td>
        <td>6/3/2020</td>
        <td>Red</td>
        <th>20</th>
      </tr>
    </tbody>
    <tfoot>
      <tr>
        <th></th>
        <td>Name</td>
        <td>Job</td>
        <td>company</td>
        <td>location</td>
        <td>Last Login</td>
        <td>Favorite Color</td>
        <th></th>
      </tr>
    </tfoot>
  </table>
</div>

```html
<div class="overflow-x-auto h-96 w-96">
  <table class="$$table $$table-xs $$table-pin-rows $$table-pin-cols">
    <thead>
      <tr>
        <th></th>
        <td>Name</td>
        <td>Job</td>
        <td>company</td>
        <td>location</td>
        <td>Last Login</td>
        <td>Favorite Color</td>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <th>1</th>
        <td>Cy Ganderton</td>
        <td>Quality Control Specialist</td>
        <td>Littel, Schaden and Vandervort</td>
        <td>Canada</td>
        <td>12/16/2020</td>
        <td>Blue</td>
        <th>1</th>
      </tr>
      <tr>
        <th>2</th>
        <td>Hart Hagerty</td>
        <td>Desktop Support Technician</td>
        <td>Zemlak, Daniel and Leannon</td>
        <td>United States</td>
        <td>12/5/2020</td>
        <td>Purple</td>
        <th>2</th>
      </tr>
      <tr>
        <th>3</th>
        <td>Brice Swyre</td>
        <td>Tax Accountant</td>
        <td>Carroll Group</td>
        <td>China</td>
        <td>8/15/2020</td>
        <td>Red</td>
        <th>3</th>
      </tr>
      <tr>
        <th>4</th>
        <td>Marjy Ferencz</td>
        <td>Office Assistant I</td>
        <td>Rowe-Schoen</td>
        <td>Russia</td>
        <td>3/25/2021</td>
        <td>Crimson</td>
        <th>4</th>
      </tr>
      <tr>
        <th>5</th>
        <td>Yancy Tear</td>
        <td>Community Outreach Specialist</td>
        <td>Wyman-Ledner</td>
        <td>Brazil</td>
        <td>5/22/2020</td>
        <td>Indigo</td>
        <th>5</th>
      </tr>
      <tr>
        <th>6</th>
        <td>Irma Vasilik</td>
        <td>Editor</td>
        <td>Wiza, Bins and Emard</td>
        <td>Venezuela</td>
        <td>12/8/2020</td>
        <td>Purple</td>
        <th>6</th>
      </tr>
      <tr>
        <th>7</th>
        <td>Meghann Durtnal</td>
        <td>Staff Accountant IV</td>
        <td>Schuster-Schimmel</td>
        <td>Philippines</td>
        <td>2/17/2021</td>
        <td>Yellow</td>
        <th>7</th>
      </tr>
      <tr>
        <th>8</th>
        <td>Sammy Seston</td>
        <td>Accountant I</td>
        <td>O'Hara, Welch and Keebler</td>
        <td>Indonesia</td>
        <td>5/23/2020</td>
        <td>Crimson</td>
        <th>8</th>
      </tr>
      <tr>
        <th>9</th>
        <td>Lesya Tinham</td>
        <td>Safety Technician IV</td>
        <td>Turner-Kuhlman</td>
        <td>Philippines</td>
        <td>2/21/2021</td>
        <td>Maroon</td>
        <th>9</th>
      </tr>
      <tr>
        <th>10</th>
        <td>Zaneta Tewkesbury</td>
        <td>VP Marketing</td>
        <td>Sauer LLC</td>
        <td>Chad</td>
        <td>6/23/2020</td>
        <td>Green</td>
        <th>10</th>
      </tr>
      <tr>
        <th>11</th>
        <td>Andy Tipple</td>
        <td>Librarian</td>
        <td>Hilpert Group</td>
        <td>Poland</td>
        <td>7/9/2020</td>
        <td>Indigo</td>
        <th>11</th>
      </tr>
      <tr>
        <th>12</th>
        <td>Sophi Biles</td>
        <td>Recruiting Manager</td>
        <td>Gutmann Inc</td>
        <td>Indonesia</td>
        <td>2/12/2021</td>
        <td>Maroon</td>
        <th>12</th>
      </tr>
      <tr>
        <th>13</th>
        <td>Florida Garces</td>
        <td>Web Developer IV</td>
        <td>Gaylord, Pacocha and Baumbach</td>
        <td>Poland</td>
        <td>5/31/2020</td>
        <td>Purple</td>
        <th>13</th>
      </tr>
      <tr>
        <th>14</th>
        <td>Maribeth Popping</td>
        <td>Analyst Programmer</td>
        <td>Deckow-Pouros</td>
        <td>Portugal</td>
        <td>4/27/2021</td>
        <td>Aquamarine</td>
        <th>14</th>
      </tr>
      <tr>
        <th>15</th>
        <td>Moritz Dryburgh</td>
        <td>Dental Hygienist</td>
        <td>Schiller, Cole and Hackett</td>
        <td>Sri Lanka</td>
        <td>8/8/2020</td>
        <td>Crimson</td>
        <th>15</th>
      </tr>
      <tr>
        <th>16</th>
        <td>Reid Semiras</td>
        <td>Teacher</td>
        <td>Sporer, Sipes and Rogahn</td>
        <td>Poland</td>
        <td>7/30/2020</td>
        <td>Green</td>
        <th>16</th>
      </tr>
      <tr>
        <th>17</th>
        <td>Alec Lethby</td>
        <td>Teacher</td>
        <td>Reichel, Glover and Hamill</td>
        <td>China</td>
        <td>2/28/2021</td>
        <td>Khaki</td>
        <th>17</th>
      </tr>
      <tr>
        <th>18</th>
        <td>Aland Wilber</td>
        <td>Quality Control Specialist</td>
        <td>Kshlerin, Rogahn and Swaniawski</td>
        <td>Czech Republic</td>
        <td>9/29/2020</td>
        <td>Purple</td>
        <th>18</th>
      </tr>
      <tr>
        <th>19</th>
        <td>Teddie Duerden</td>
        <td>Staff Accountant III</td>
        <td>Pouros, Ullrich and Windler</td>
        <td>France</td>
        <td>10/27/2020</td>
        <td>Aquamarine</td>
        <th>19</th>
      </tr>
      <tr>
        <th>20</th>
        <td>Lorelei Blackstone</td>
        <td>Data Coordinator</td>
        <td>Witting, Kutch and Greenfelder</td>
        <td>Kazakhstan</td>
        <td>6/3/2020</td>
        <td>Red</td>
        <th>20</th>
      </tr>
    </tbody>
    <tfoot>
      <tr>
        <th></th>
        <td>Name</td>
        <td>Job</td>
        <td>company</td>
        <td>location</td>
        <td>Last Login</td>
        <td>Favorite Color</td>
        <th></th>
      </tr>
    </tfoot>
  </table>
</div>
```
# Side bar

---
title: Drawer sidebar
desc: Drawer is a grid layout that can show/hide a sidebar on the left or right side of the page.
source: https://raw.githubusercontent.com/saadeghi/daisyui/refs/heads/master/packages/daisyui/src/components/drawer.css
layout: components
classnames:
component:
- class: drawer
desc: The wrapper for sidebar and content
part:
- class: drawer-toggle
desc: The hidden checkbox that controls the state of drawer
- class: drawer-content
desc: Content part
- class: drawer-side
desc: Sidebar part
- class: drawer-overlay
desc: Label that covers the page when drawer is open
placement:
- class: drawer-end
desc: puts drawer to the other side
modifier:
- class: drawer-open
desc: Forces the drawer to be open
variant:
- class: "is-drawer-open:"
desc: Applies styles when the drawer is open
- class: "is-drawer-close:"
desc: Applies styles when the drawer is closed
---

<script>
  import Component from "$components/Component.svelte"
  import Translate from "$components/Translate.svelte"
</script>

### Structure

Drawer is a grid layout that can show/hide a sidebar on the left or right side of the page, based on the screen size or based on the value of a `drawer-toggle` checkbox.  
Drawer must be the parent element of the content and sidebar.

```json:Structure
.drawer // The root container
  ├── .drawer-toggle // A hidden checkbox to toggle the visibility of the sidebar
  ├── .drawer-content // All your page content goes here
  │    ╰── // navbar, content, footer
  │
  ╰── .drawer-side // Sidebar wrapper
       ├── .drawer-overlay // A dark overlay that covers the whole page when the drawer is open
       ╰── // Sidebar content (menu or anything)
```

### Functionality

Drawer sidebar is hidden by default.
You can make it visible on larger screens using `lg:drawer-open` class (or using other responsive prefixes: sm, md, lg, xl)

You can check/uncheck the checkbox using JavaScript or by clicking the `label` tag which is assigned to the hidden checkbox

> :INFO:
>
> Opening a drawer adds a [scrollbar-gutter](https://developer.mozilla.org/en-US/docs/Web/CSS/scrollbar-gutter) to the page to avoid layout shift on operating systems that have a fixed scrollbar.
> On recent Chromium based browsers vertical scrollbar presence is detected automatically. On Safari and on mobile devices the scrollbar is displayed as overlay so there will not be gutter. On Firefox you need to detect the presence of vertical scrollbar and set the `scrollbar-gutter: stable` or `scrollbar-gutter: unset` on `:root` element yourself.
> If you don't want to use this feature, [you can exclude `rootscrollgutter`](/docs/config/#exclude).

### ~Drawer sidebar

<div class="drawer h-56 rounded overflow-hidden">
  <input id="my-drawer-1" type="checkbox" class="drawer-toggle" />
  <div class="flex flex-col items-center justify-center drawer-content">
    <label for="my-drawer-1" class="btn drawer-button">Open drawer</label>
  </div>
  <div class="drawer-side z-1002">
    <label for="my-drawer-1" aria-label="close sidebar" class="drawer-overlay"></label>
    <ul class="menu p-4 w-60 md:w-80 min-h-full bg-base-200">
      <li><button>Sidebar Item 1</button></li>
      <li><button>Sidebar Item 2</button></li>
    </ul>
  </div>
</div>

```html
<div class="$$drawer">
  <input id="my-drawer-1" type="checkbox" class="$$drawer-toggle" />
  <div class="$$drawer-content">
    <!-- Page content here -->
    <label for="my-drawer-1" class="$$btn $$$$drawer-button">Open drawer</label>
  </div>
  <div class="$$drawer-side">
    <label for="my-drawer-1" aria-label="close sidebar" class="$$drawer-overlay"></label>
    <ul class="$$menu bg-base-200 min-h-full w-80 p-4">
      <!-- Sidebar content here -->
      <li><a>Sidebar Item 1</a></li>
      <li><a>Sidebar Item 2</a></li>
    </ul>
  </div>
</div>
```

### ~Navbar menu for desktop + sidebar drawer for mobile

#### Change screen size to show/hide menu

<div class="drawer h-56 rounded overflow-hidden">
  <input id="my-drawer-2" type="checkbox" class="drawer-toggle" />
  <div class="flex flex-col drawer-content">
    <div class="w-full navbar bg-base-300">
      <div class="flex-none lg:hidden">
        <label for="my-drawer-2" aria-label="open sidebar" class="btn btn-square btn-ghost">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" class="inline-block w-6 h-6 stroke-current"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>
        </label>
      </div>
      <div class="flex-1 px-2 mx-2">Navbar Title</div>
      <div class="flex-none hidden lg:block">
        <ul class="menu menu-horizontal">
          <li><button>Navbar Item 1</button></li>
          <li><button>Navbar Item 2</button></li>
        </ul>
      </div>
    </div>
    <div class="flex justify-center items-center grow">Content</div>
  </div>
  <div class="drawer-side z-1002">
    <label for="my-drawer-2" aria-label="close sidebar" class="drawer-overlay"></label>
    <ul class="p-4 menu w-60 md:w-80 min-h-full bg-base-200">
      <li><button>Sidebar Item 1</button></li>
      <li><button>Sidebar Item 2</button></li>
    </ul>
  </div>
</div>

```html
<div class="$$drawer">
  <input id="my-drawer-2" type="checkbox" class="$$drawer-toggle" />
  <div class="$$drawer-content flex flex-col">
    <!-- Navbar -->
    <div class="$$navbar bg-base-300 w-full">
      <div class="flex-none lg:hidden">
        <label for="my-drawer-2" aria-label="open sidebar" class="$$btn $$btn-square $$btn-ghost">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            class="inline-block h-6 w-6 stroke-current"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M4 6h16M4 12h16M4 18h16"
            ></path>
          </svg>
        </label>
      </div>
      <div class="mx-2 flex-1 px-2">Navbar Title</div>
      <div class="hidden flex-none lg:block">
        <ul class="$$menu $$menu-horizontal">
          <!-- Navbar menu content here -->
          <li><a>Navbar Item 1</a></li>
          <li><a>Navbar Item 2</a></li>
        </ul>
      </div>
    </div>
    <!-- Page content here -->
    Content
  </div>
  <div class="$$drawer-side">
    <label for="my-drawer-2" aria-label="close sidebar" class="$$drawer-overlay"></label>
    <ul class="$$menu bg-base-200 min-h-full w-80 p-4">
      <!-- Sidebar content here -->
      <li><a>Sidebar Item 1</a></li>
      <li><a>Sidebar Item 2</a></li>
    </ul>
  </div>
</div>
```

### ~Responsive: Sidebar is always visible on large screen, can be toggled on small screen

#### Sidebar is always visible on large screen, can be toggled on small screen because of lg:drawer-open class

<div class="drawer lg:drawer-open h-56 rounded overflow-hidden">
  <input id="my-drawer-3" type="checkbox" class="drawer-toggle" />
  <div class="flex flex-col items-center justify-center drawer-content">
    <label for="my-drawer-3" class="btn drawer-button lg:hidden">Open drawer</label>
  </div>
  <div class="drawer-side max-lg:z-1002">
    <label for="my-drawer-3" aria-label="close sidebar" class="drawer-overlay"></label>
    <ul class="menu p-4 w-60 md:w-80 min-h-full bg-base-200">
      <li><button>Sidebar Item 1</button></li>
      <li><button>Sidebar Item 2</button></li>
    </ul>
  </div>
</div>

```html
<div class="$$drawer lg:$$drawer-open">
  <input id="my-drawer-3" type="checkbox" class="$$drawer-toggle" />
  <div class="$$drawer-content flex flex-col items-center justify-center">
    <!-- Page content here -->
    <label for="my-drawer-3" class="$$btn $$$$drawer-button lg:hidden">
      Open drawer
    </label>
  </div>
  <div class="$$drawer-side">
    <label for="my-drawer-3" aria-label="close sidebar" class="$$drawer-overlay"></label>
    <ul class="$$menu bg-base-200 min-h-full w-80 p-4">
      <!-- Sidebar content here -->
      <li><a>Sidebar Item 1</a></li>
      <li><a>Sidebar Item 2</a></li>
    </ul>
  </div>
</div>
```


### ~Responsive collapsible Icon-only drawer sidebar. Using is-drawer-close and is-drawer-open

#### In this example instead of completely hiding the drawer sidebar, we only show the icons when the drawer is closed. To add styles based on the state of the drawer, we use the `is-drawer-open` and `is-drawer-close` variants. For example `is-drawer-close:hidden` or `is-drawer-open:w-80`. Also we have tooltips when the drawer is closed and a switch button to open/close the drawer and rotates the button icon based on the state of the drawer.

<div class="drawer lg:drawer-open h-80">
  <input id="my-drawer-4" type="checkbox" class="drawer-toggle" />
  <div class="drawer-content">
    <!-- Navbar -->
    <nav class="navbar w-full bg-base-300">
      <label for="my-drawer-4" aria-label="open sidebar" class="btn btn-square btn-ghost">
        <!-- Sidebar toggle icon -->
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" stroke-linejoin="round" stroke-linecap="round" stroke-width="2" fill="none" stroke="currentColor" class="my-1.5 inline-block size-4"><path d="M4 4m0 2a2 2 0 0 1 2 -2h12a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2z"></path><path d="M9 4v16"></path><path d="M14 10l2 2l-2 2"></path></svg>
      </label>
      <div class="px-4">Navbar Title</div>
    </nav>
    <!-- Page content here -->
    <div class="p-4">Page Content</div>
  </div>
  <div class="drawer-side max-lg:top-16 lg:h-80 is-drawer-close:overflow-visible">
    <label for="my-drawer-4" aria-label="close sidebar" class="drawer-overlay"></label>
    <div class="flex min-h-full flex-col items-start bg-base-200 is-drawer-close:w-14 is-drawer-open:w-64">
      <!-- Sidebar content here -->
      <ul class="menu w-full grow">
        <!-- List item -->
        <li>
          <button class="is-drawer-close:tooltip is-drawer-close:tooltip-right" data-tip="Homepage">
            <!-- Home icon -->
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" stroke-linejoin="round" stroke-linecap="round" stroke-width="2" fill="none" stroke="currentColor" class="my-1.5 inline-block size-4"><path d="M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8"></path><path d="M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path></svg>
            <span class="is-drawer-close:hidden">Homepage</span>
          </button>
        </li>
        <!-- List item -->
        <li>
          <button class="is-drawer-close:tooltip is-drawer-close:tooltip-right" data-tip="Settings">
            <!-- Settings icon -->
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" stroke-linejoin="round" stroke-linecap="round" stroke-width="2" fill="none" stroke="currentColor" class="my-1.5 inline-block size-4"><path d="M20 7h-9"></path><path d="M14 17H5"></path><circle cx="17" cy="17" r="3"></circle><circle cx="7" cy="7" r="3"></circle></svg>
            <span class="is-drawer-close:hidden">Settings</span>
          </button>
        </li>
      </ul>
    </div>
  </div>
</div>

```html
<div class="$$drawer lg:$$drawer-open">
  <input id="my-drawer-4" type="checkbox" class="$$drawer-toggle" />
  <div class="$$drawer-content">
    <!-- Navbar -->
    <nav class="$$navbar w-full bg-base-300">
      <label for="my-drawer-4" aria-label="open sidebar" class="$$btn $$btn-square $$btn-ghost">
        <!-- Sidebar toggle icon -->
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" stroke-linejoin="round" stroke-linecap="round" stroke-width="2" fill="none" stroke="currentColor" class="my-1.5 inline-block size-4"><path d="M4 4m0 2a2 2 0 0 1 2 -2h12a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2z"></path><path d="M9 4v16"></path><path d="M14 10l2 2l-2 2"></path></svg>
      </label>
      <div class="px-4">Navbar Title</div>
    </nav>
    <!-- Page content here -->
    <div class="p-4">Page Content</div>
  </div>

  <div class="$$drawer-side $$is-drawer-close:overflow-visible">
    <label for="my-drawer-4" aria-label="close sidebar" class="$$drawer-overlay"></label>
    <div class="flex min-h-full flex-col items-start bg-base-200 $$is-drawer-close:w-14 $$is-drawer-open:w-64">
      <!-- Sidebar content here -->
      <ul class="$$menu w-full grow">
        <!-- List item -->
        <li>
          <button class="$$is-drawer-close:tooltip $$is-drawer-close:tooltip-right" data-tip="Homepage">
            <!-- Home icon -->
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" stroke-linejoin="round" stroke-linecap="round" stroke-width="2" fill="none" stroke="currentColor" class="my-1.5 inline-block size-4"><path d="M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8"></path><path d="M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path></svg>
            <span class="$$is-drawer-close:hidden">Homepage</span>
          </button>
        </li>

        <!-- List item -->
        <li>
          <button class="$$is-drawer-close:tooltip $$is-drawer-close:tooltip-right" data-tip="Settings">
            <!-- Settings icon -->
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" stroke-linejoin="round" stroke-linecap="round" stroke-width="2" fill="none" stroke="currentColor" class="my-1.5 inline-block size-4"><path d="M20 7h-9"></path><path d="M14 17H5"></path><circle cx="17" cy="17" r="3"></circle><circle cx="7" cy="7" r="3"></circle></svg>
            <span class="$$is-drawer-close:hidden">Settings</span>
          </button>
        </li>
      </ul>
    </div>
  </div>
</div>
```


### ~Drawer sidebar that opens from right side of page

<div class="drawer drawer-end h-56 rounded overflow-hidden">
  <input id="my-drawer-5" type="checkbox" class="drawer-toggle" />
  <div class="flex flex-col items-center justify-center drawer-content">
    <label for="my-drawer-5" class="btn drawer-button">Open drawer</label>
  </div>
  <div class="drawer-side z-1002">
    <label for="my-drawer-5" aria-label="close sidebar" class="drawer-overlay"></label>
    <ul class="menu p-4 w-60 md:w-80 min-h-full bg-base-200">
      <li><button>Sidebar Item 1</button></li>
      <li><button>Sidebar Item 2</button></li>
    </ul>
  </div>
</div>

```html
<div class="$$drawer $$drawer-end">
  <input id="my-drawer-5" type="checkbox" class="$$drawer-toggle" />
  <div class="$$drawer-content">
    <!-- Page content here -->
    <label for="my-drawer-5" class="$$drawer-button $$btn $$btn-primary">Open drawer</label>
  </div>
  <div class="$$drawer-side">
    <label for="my-drawer-5" aria-label="close sidebar" class="$$drawer-overlay"></label>
    <ul class="$$menu bg-base-200 min-h-full w-80 p-4">
      <!-- Sidebar content here -->
      <li><a>Sidebar Item 1</a></li>
      <li><a>Sidebar Item 2</a></li>
    </ul>
  </div>
</div>
```
